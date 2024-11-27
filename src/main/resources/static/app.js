const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/kafkatest'
});

stompClient.onConnect = (frame) => {
    console.log('Connected: ' + frame);
    let i = 0;
    stompClient.subscribe('/topic/messages', (ret) => {
        console.log(`${i++}th message in ${new Date()}`)
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function hello(ret) {
    $('conn').append("<p>" + ret + "</p>");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    console.log("Disconnected");
}

function sendMessage() {
    const start = Date.now();
    $.get('/preWebsocket')
        .done(function(data) {
            for(let i=0; i<100; i++) {
                stompClient.publish({
                    destination: '/app/chatWebsocket',
                    body: JSON.stringify(data) + `$${i}`,
                })
            }
        });
}

function sendMessageWithSocket() {
    $.get('/preWebsocket')
        .done(function(data) {
            console.log(`first message sent in ${new Date()}`);
            for(let i=0; i<100000; i++) {
                stompClient.publish({
                    destination: `/app/withMillionMessages`,
                    body: JSON.stringify({
                        "chatMessageType": "TEXT",
                        "image": "",
                        "emoticon": "",
                        "message": `hello ${i}th message`,
                        "chatRoomId": data,
                        "order": i
                    }),
                })
            }
        });
}

function kafka() {
    const start = Date.now();
    $.get('/preWebsocket')
        .done(function(data) {
            console.log(`first message from kafka sent in ${new Date()}`)
            for(let i=0; i<3000; i++) {
                $.ajax({
                    type: 'post',
                    url: `/millionMessages`,
                    data: JSON.stringify({
                        "chatMessageType": "TEXT",
                        "image": "",
                        "emoticon": "",
                        "message": `hello ${i}th message`,
                        "chatRoomId": data,
                        "order": i
                    }),
                    contentType: 'application/json; charset=utf-8',
                });
            }
        })
}