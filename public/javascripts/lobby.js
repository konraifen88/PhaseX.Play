/**
 * Created by tabuechn on 07.01.2016.
 */

var lobbyApp = angular.module('lobby', []);
lobbyApp.controller('LobbyCtrl', function ($scope, $http) {

    var ws = new WebSocket("ws://localhost:9000/lobby/socket");

    $scope.updateLobby = function(data) {
        $scope.users = data.map.allUsers;
        $scope.numberOfUsers = data.map.allUsers.length;
        $scope.games = data.map.games;
    };

    $scope.update = function () {
        $http.get('/lobby/update').success(function (data) {
            $scope.updateLobby(data);
        });
    };

    $scope.addGame = function () {
        /*
        $http.get('/lobby/addGame').success(function(data) {
            console.log("game added");
            $scope.updateLobby(data);
        });*/
        ws.send('addGame')
    };

    $scope.clickedOnGame = function (gameNumber) {
        if($scope.games[gameNumber].numberOfPlayers < 2) {
            $http.get('/lobby/joinGame/' + gameNumber).success(function(data) {
               console.log("player added to game " + gameNumber);
                $scope.updateLobby(data);
            });
            ws.send("joinGame " + gameNumber);
        }
    };

    $scope.startGame = function (gameNumber) {
        $http.get('/lobby/startGame/' + gameNumber).success(function() {
            window.location.replace("/ngApp");
        })
    };

    $http.get('/lobby/update').success(function (data) {
        console.log(data);
        $scope.users = data.map.allUsers;
        $scope.numberOfUsers = data.map.allUsers.length;
    });

    ws.onmessage = function(mesg) {
        /*var jsonData = jQuery.parseJSON(mesg.data);
        console.log(jsonData);
        $scope.updateLobby(jsonData);*/
        $scope.update();
    }

} );