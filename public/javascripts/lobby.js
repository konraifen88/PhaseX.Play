/**
 * Created by tabuechn on 07.01.2016.
 */

var lobbyApp = angular.module('lobby', []);
console.log("hallo");
lobbyApp.controller('LobbyCtrl',["$http", "$scope", function ($scope, $http) {

    $scope.update = function () {
        console.log("test");
        $http.get('/lobby/update').success(function (data) {
            console.log(data);
        });
    };


} ]);