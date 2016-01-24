/**
 * If everything works right this class was
 * created by Konraifen88 on 14.01.2016.
 * If it doesn't work I don't know who the hell wrote it.
 */

var navBar = angular.module('navBar', []);
navBar.controller('navCtrl', function ($scope, $http) {
    $scope.isHomePage = function () {
        return !(window.location.pathname === "/");
    };

    $scope.isUserLoggedIn = function () {
        return $("#playerName").text() != "";
    };

    $scope.notInLobby = function () {
        return window.location.pathname.search("\/chat\/+") != -1;
    };

    $scope.notInGame = function () {
        return window.location.pathname.search("\/ngApp\/+") != -1;
    }

    $scope.hideLoginIcons = function () {
        if ($scope.isUserLoggedIn()) {
            $("authIcons").hide()
        }
    }

});
angular.element(document).ready(function() {
    angular.bootstrap(document.getElementById("mainNav"),["navBar"]);
});


