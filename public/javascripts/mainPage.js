/**
 * Created by Konraifen88 on 13.01.2016.
 */

var mainPage = angular.module('mainPage', []);
mainPage.controller('mainCtrl', function ($scope) {

    $scope.getLobbies = function () {
        $scope.lobbies = JSON.parse($("#lobbyData").text());
    };
    $scope.getLobbies();
});

