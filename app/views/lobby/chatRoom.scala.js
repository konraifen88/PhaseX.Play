@(username: String, roomName: String)

$(function () {
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
    var chatSocket = new WS("@routes.Chat.chat(username, roomName).webSocketURL(request)");

    $("#startButton").click(function () {
        chatSocket.send(JSON.stringify(
            {text: "GAME"}
        ));
    });

    var sendMessage = function () {
        chatSocket.send(JSON.stringify(
            {text: $("#talk").val()}
        ));
        $("#talk").val('')
    };

    var receiveEvent = function (event) {
        var data = JSON.parse(event.data);

        // Handle errors
        if (data.error) {
            chatSocket.close();
            $("#onError span").text(data.error);
            $("#onError").show();
            return
        } else {
            $("#onChat").show()
        }
        if (data.message === "GAME") {
            window.location.replace("../ngApp/" + '@roomName');
        } else {
            // Create the message element
            var el = $('<div class="message"><span></span><p></p></div>');
            $("span", el).text(data.user);
            $("p", el).text(data.message);
            $(el).addClass(data.kind);
            if (data.user == '@username') $(el).addClass('me');
            $('#messages').append(el);

            // Update the members list
            $("#members").html('');
            $(data.members).each(function () {
                var li = document.createElement('li');
                li.textContent = this;
                $("#members").append(li);
            })
            $('#messages')[0].scrollTop = 9999;
        }

    };

    var handleReturnKey = function (e) {
        if (e.charCode == 13 || e.keyCode == 13) {
            e.preventDefault();
            sendMessage()
        }
    };

    var errorHandler = function (event) {
        console.log("Error!");
        console.log(event);
    };

    $("#talk").keypress(handleReturnKey);

    chatSocket.onmessage = receiveEvent;
    chatSocket.onerror = errorHandler;
    console.log("init ready");
    (function() {
        console.log("stayingAlive");
        if(chatSocket.readyState == 1) {
            console.log("sending staying Alive");
            var payload = new Object();
            payload.stay = "stayingAlive";
            chatSocket.send(JSON.stringify(payload));
            setTimeout(arguments.callee,30000);
        }else if(chatSocket.readyState == 0) {
            setTimeout(arguments.callee,200);
        } else {
            console.log("Socket closed.")
        }
    })();

});
