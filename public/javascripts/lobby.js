/**
 * Created by tabuechn on 07.01.2016.
 */

var lobbyApp = angular.module('lobby', []);
lobbyApp.controller('LobbyCtrl', function ($scope, $http) {

    $scope.update = function () {
        $http.get('/lobby/update').success(function (data) {
            console.log(data);
            $scope.users = data.map.allUsers;
            $scope.numberOfUsers = data.map.allUsers.length;
        });
    };

    $http.get('/lobby/update').success(function (data) {
        console.log(data);
        $scope.users = data.map.allUsers;
        $scope.numberOfUsers = data.map.allUsers.length;
    });

} );