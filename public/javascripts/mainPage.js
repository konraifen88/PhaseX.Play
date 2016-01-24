/**
 * If everything works right this class was
 * created by Konraifen88 on 13.01.2016.
 * If it doesn't work I don't know who the hell wrote it.
 */

var mainPage = angular.module('mainPage', []);
mainPage.controller('mainCtrl', function ($scope) {

    var sock;

    $scope.getSocket = function() {
        var origin = window.location.origin.replace("https", "http");
        origin = origin.replace("http", "ws");
        sock = new WebSocket(origin + "/lobbySocket");
        console.log(sock);
        sock.onopen =function() {
            console.log("socket initialised");
            sock.send("update");
        };
        sock.onmessage = function(message) {
            console.log(message);
            $scope.lobbies = JSON.parse(message.data);
            $scope.separateLobbies();
        };
    };

    $scope.getSocket();

    $scope.joinLobby = function () {
        var newLobbyName = $('#newLobbyInput').val();
        var lobbyRegExp = new RegExp('^[a-zA-Z0-9]+$');
        if(lobbyRegExp.test(newLobbyName)) {
            window.location.replace('/chat/' + $('#newLobbyInput').val());
        } else {
            alert("Roomname can only contain letters and numbers");
        }
        //window.location.replace('/chat/' + $('#newLobbyInput').val());
    };

    $scope.isError = function () {
        return $("#errorState").text() != "";
    }

    $('#newLobbyBtn').on('click', $scope.joinLobby);
    //$scope.freeLobbies = [];

    $scope.separateLobbies = function () {
        $scope.freeLobbies = [];
        $scope.fullLobbies = [];
        console.log("lobbies %o", $scope.lobbies);
        angular.forEach($scope.lobbies, function (value, key) {
            if (parseInt(value) <= 1) {
                $scope.freeLobbies.push(key);
            } else {
                $scope.fullLobbies.push(key);
            }
        });
        $scope.$apply();
        console.log("free lobbies %o", $scope.freeLobbies);
    };
    (function() {
        console.log("stayingAlive");
        if(sock.readyState == 1) {
            console.log("sending staying Alive");
            var payload = new Object();
            payload.stay = "stayingAlive";
            sock.send(JSON.stringify(payload));
            setTimeout(arguments.callee,30000);
        }else if(sock.readyState == 0) {
            setTimeout(arguments.callee,200);
        } else {
            console.log("Socket closed.")
        }
    })();

});
angular.element(document).ready(function() {
    angular.bootstrap(document.getElementById("lobbyApp"),["mainPage"]);
});

