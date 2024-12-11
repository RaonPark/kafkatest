package com.example.kafkatest.avro;

import example.avro.User;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.SchemaParser;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.File;
import java.io.IOException;

public class AvroMain {
    public static void main(String[] args) {
        User user1 = new User();
        user1.setName("Alyssa");
        user1.setFavoriteNumber(256);

        User user2 = new User("Ben", 7, "red");

        User user3 = User.newBuilder()
                .setName("Charlie")
                .setFavoriteColor("blue")
                .setFavoriteNumber(null)
                .build();

        // Serializing Data
        try {
            DatumWriter<User> userDatumWriter = new SpecificDatumWriter<>(User.class);
            DataFileWriter<User> dataFileWriter = new DataFileWriter<>(userDatumWriter);
            dataFileWriter.create(user1.getSchema(), new File("users.avro"));
            dataFileWriter.append(user1);
            dataFileWriter.append(user2);
            dataFileWriter.append(user3);
            dataFileWriter.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        // Deserializing Data
        try {
            DatumReader<User> userDatumReader = new SpecificDatumReader<>(User.class);
            DataFileReader<User> dataFileReader = new DataFileReader<>(new File("users.avro"), userDatumReader);
            User user = null;
            while(dataFileReader.hasNext()) {
                // User Object 를 next() 를 사용하여 재사용이 가능하다.
                // file 에 있는 많은 Object 들을 할당하고 가비지 콜렉팅을 하는데 도움을 준다.
                user = dataFileReader.next();

                System.out.println(user);
            }
        } catch(IOException e) {
            throw new AvroRuntimeException(e);
        }

        // Ser / Des without code generation
        try {
            Schema schema = new SchemaParser().parse(new File("src/main/avro/example.avsc")).mainSchema();
            GenericRecord genericRecordUser1 = new GenericData.Record(schema);
            genericRecordUser1.put("name", "Alyssa");
            genericRecordUser1.put("favorite_number", 256);

            // 만약 없는 속성을 할당하려고 하면, 예를 들어 "favorite_animal" : "cat" 을 넣는 경우
            // AvroRuntimeException 이 발생한다.
            GenericRecord genericRecordUser2 = new GenericData.Record(schema);
            genericRecordUser2.put("name", "Ben");
            genericRecordUser2.put("favorite_number", 7);
            genericRecordUser2.put("favorite_color", "red");

            // ser
            File file = new File("users.avro");
            DatumWriter<GenericRecord> genericRecordDatumWriter = new GenericDatumWriter<>(schema);
            DataFileWriter<GenericRecord> genericRecordDataFileWriter = new DataFileWriter<>(genericRecordDatumWriter);
            genericRecordDataFileWriter.create(schema, file);
            genericRecordDataFileWriter.append(genericRecordUser1);
            genericRecordDataFileWriter.append(genericRecordUser2);
            genericRecordDataFileWriter.close();

            // des
            DatumReader<GenericRecord> genericRecordDatumReader = new GenericDatumReader<>(schema);
            DataFileReader<GenericRecord> genericRecordDataFileReader = new DataFileReader<>(file, genericRecordDatumReader);
            GenericRecord genericRecord = null;
            while(genericRecordDataFileReader.hasNext()) {
                genericRecord = genericRecordDataFileReader.next();
                System.out.println(genericRecord);
            }
        } catch (IOException e) {
            throw new AvroRuntimeException(e);
        }

    }
}
