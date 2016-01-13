/**
 * Created by Konraifen88 on 13.01.2016.
 */

var mainPage = angular.module('mainPage', []);
mainPage.controller('mainCtrl', function ($scope) {

    $scope.getLobbies = function () {
        $scope.lobbiesObj = JSON.parse($("#lobbyData").text());
        $scope.lobbies = [];
        for (var i = 0; i < Object.keys($scope.lobbiesObj).length; i++) {
            var key = Object.keys($scope.lobbiesObj)[i];
            var val = $scope.lobbiesObj[key];
            $scope.lobbies.push({
                lobbyName: key,
                users: val
            });
        }
        console.log($scope.lobbies);
    };
    $scope.lobbies = [];
    $scope.getLobbies();
});

