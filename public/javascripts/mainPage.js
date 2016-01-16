/**
 * If everything works right this class was
 * created by Konraifen88 on 13.01.2016.
 * If it doesn't work I don't know who the hell wrote it.
 */

var mainPage = angular.module('mainPage', []);
mainPage.controller('mainCtrl', function ($scope) {

    $scope.getLobbies = function () {
        $scope.lobbies = JSON.parse($("#lobbyData").text());
    };

    $('#newLobbyBtn').on('click', function (e) {
        var newLobbyName = $('#newLobbyInput').val();
        var lobbyRegExp = new RegExp('^[a-zA-Z0-9]+$');
        if(lobbyRegExp.test(newLobbyName)) {
            window.location.replace('/chat/' + $('#newLobbyInput').val());
        } else {
            alert("Roomname can only contain letters and numbers");
        }
        //window.location.replace('/chat/' + $('#newLobbyInput').val());
    });

    $scope.separateLobbies = function () {
        $scope.freeLobbies = [];
        $scope.fullLobbies = [];
        angular.forEach($scope.lobbies, function (value, key) {
            if (parseInt(value) <= 1) {
                $scope.freeLobbies.push(key);
            } else {
                $scope.fullLobbies.push(key);
            }
        });

    };
    $scope.getLobbies();
    $scope.separateLobbies();

});
angular.element(document).ready(function() {
    angular.bootstrap(document.getElementById("lobbyApp"),["mainPage"]);
});

