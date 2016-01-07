/**
 * Created by tabuechn on 07.01.2016.
 */

var lobbyApp = angular.module('lobby', []);
lobbyApp.controller('LobbyCtrl', function ($scope, $http) {

    $scope.updateLobby = function(data) {
        console.log(data);
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
        $http.get('/lobby/addGame').success(function(data) {
            console.log("game added");
            $scope.updateLobby(data);
        });
    };

    $scope.clickedOnGame = function (gameNumber) {
        console.log("lobby clicked" + gameNumber);
        console.log($scope.games[gameNumber]);
        if($scope.games[gameNumber].numberOfPlayers < 2) {
            $http.get('/lobby/joinGame/' + gameNumber).success(function(data) {
               console.log("player added to game " + gameNumber);
                $scope.updateLobby(data);
            });
        }
    };

    $http.get('/lobby/update').success(function (data) {
        console.log(data);
        $scope.users = data.map.allUsers;
        $scope.numberOfUsers = data.map.allUsers.length;
    });

} );