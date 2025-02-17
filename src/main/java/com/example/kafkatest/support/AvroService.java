package com.example.kafkatest.support;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.util.ByteBufferOutputStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

@Service
public class AvroService {
    public static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerdeForValue(Map<String, Object> configMap) {
        SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();
        // true 면 key 를 위한 serde, false 면 value 를 위한 serde
        specificAvroSerde.configure(configMap, false);
        return specificAvroSerde;
    }

    // 이 경우는 미리 준비한 스키마가 없이 직접 pojo에서 avro로 변환할 수 있다.
    // 이 방법은 직접 바이너리 데이터를 IO로 쓰는 것과 같은 역할을 한다.
    public static GenericData.Record convertPojo2Avro(Object pojo) throws IOException {
        // 스키마를 먼저 pojo 에서 얻어온다.
        Schema schema = ReflectData.get().getSchema(pojo.getClass());
        // 그리고 DatumWriter 를 사용하여 바이너리 데이터를 쓸 걸 준비한다.
        // ReflectDatumWriter 는 Java Reflection 을 사용하여 데이터를 쓴다.
        ReflectDatumWriter<Object> writer = new ReflectDatumWriter<>(schema);
        // ByteBufferOutputStream 이나 ByteArrayOutputStream 을 사용할 수 있는데,
        // 효율적인건 ByteBufferOutputStream 이라고 한다. 우선 더 크거나 더 복잡한 avro schema 의 경우는
        // 내부적으로 array 가 계속해서 클 수 있는 ByteArray 를 쓰는 것이 좋지만
        // ByteBufferOutputStream 은 고정적인 크기의 ByteBuffer 를 사용하므로
        // 버퍼의 크기 조절과 같은 오버헤드에서 벗어날 수 있다.
        // 현재 ByteBuffer 는 기본적으로 8192 크기로 고정되어있다. 이는 약 8KB 이다.
        ByteBufferOutputStream outputStream = new ByteBufferOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);

        writer.write(pojo, encoder);
        encoder.flush();

        byte[] byteArray = convertByteBuffer2ByteArray(outputStream);

        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(byteArray, null);
        GenericDatumReader<GenericData.Record> reader = new GenericDatumReader<>(schema);

        return reader.read(null, decoder);
    }

    private static byte[] convertByteBuffer2ByteArray(ByteBufferOutputStream outputStream) {
        int totalSize = outputStream.getBufferList().stream().mapToInt(ByteBuffer::remaining).sum();
        byte[] byteArray = new byte[totalSize];

        int offset = 0;
        for(ByteBuffer buffer: outputStream.getBufferList()) {
            int bufferSize = buffer.remaining();
            buffer.get(byteArray, offset, bufferSize);
            offset += bufferSize;
        }

        return byteArray;
    }
}
