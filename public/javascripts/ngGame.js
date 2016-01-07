/**
 * Created by tabuechn on 12.12.2015.
 */

var phaseXApp = angular.module('ngApp', []);

phaseXApp.directive('card', function () {
    return {
        scope: {
            number: '@',
            color: '@'
        },
        templateUrl: 'assets/html/card.html'
    }
});

phaseXApp.directive('stackcard', function () {
    return {
        scope: {
            number: '@',
            color: '@'
        },
        templateUrl: 'assets/html/stackcard.html'
    }
});

phaseXApp.directive('pilecard', function () {
    return {
        scope: {
            number: '@',
            color: '@'
        },
        templateUrl: 'assets/html/pilecard.html'
    }
});

phaseXApp.controller('GameCtrl', function ($scope, $http) {

    $scope.getFirstAndLast = function (stack) {
        if (stack.length > 4) {
            var newStack = [];
            newStack.push(stack[0]);
            newStack.push(stack[0]);
            newStack.push(stack[1]);
            newStack.push(stack[length - 2]);
            newStack.push(stack[length - 1]);
            return newStack;
        } else {
            return stack;
        }
    };

    $scope.update = function (data) {
        $scope.debug = debug(data);
        $scope.playerCards = $scope.setPlayerCardsUnselected(data.map.playerHand);
        $scope.stack1empty = data.map.stack1.length === 0;
        $scope.stack1 = data.map.stack1;
        $scope.stack2empty = data.map.stack2.length === 0;
        $scope.stack2 = data.map.stack2;
        $scope.stack3empty = data.map.stack3.length === 0;
        $scope.stack3 = data.map.stack3;
        $scope.stack4empty = data.map.stack4.length === 0;
        $scope.stack4 = data.map.stack4;
        $scope.roundState = data.map.roundState;
        $scope.currentPlayerPhase = data.map.currentPlayerPhase;
        $scope.currentPlayerName = data.map.currentPlayerStats.name;
        $scope.opponentsCards = data.map.opponent;
        $scope.discardEmpty = data.map.discardIsEmpty;
        $scope.discardPile = data.map.discard[data.map.discard.length - 1];
        $scope.state = data.map.state;
        $scope.selectedCards = [];
        debug(data);
    };

    $scope.drawhidden = function () {
        if ($scope.state === "DrawPhase") {
            $http.get('/json/drawHidden').success(function (data) {
                /*$scope.playerCards = data.map.playerHand;
                 $scope.state = data.map.state;*/
                $scope.update(data);
            });
        }

    };

    $scope.setPlayerCardsUnselected = function (data) {
        var unselected = [];
        for (var i = 0; i < data.length; i += 1) {
            unselected.push(data[i]);
            unselected[i].selected = false;
        }
        return unselected;
    };

    $scope.discardclick = function () {
        if ($scope.state === "DrawPhase") {
            $scope.drawdiscard();
        } else if ($scope.state === "PlayerTurnFinished" || $scope.state === "PlayerTurnNotFinished") {
            $scope.discard();
        }
    };

    $scope.getSelectedCards = function () {
        var i = 0;
        var indexArray = [];
        while (i < $scope.playerCards.length) {
            if ($scope.playerCards[i].selected == true) {
                indexArray.push(i);
            }
            i += 1;
        }
        return indexArray;
    };

    $scope.discard = function () {
        var selectedCards = $scope.getSelectedCards();
        if (selectedCards.length == 1) {
            $http.get('/json/discard/' + selectedCards[0]).success(function (data) {
                $scope.update(data);
            });
        }
    };

    $scope.cardup = function (card) {
        $scope.selectedCards.push(card);
        if (card.selected == true) {
            card.selected = false;
        } else {
            card.selected = true;
        }
    };

    $scope.styleup = function (check) {
        if (check === true) {
            return "CardUp";
        } else {
            return "";
        }
    };

    $scope.drawdiscard = function () {
        $http.get('/json/drawDiscard').success(function (data) {
            $scope.update(data);
        });
    };

    $scope.stackClick = function (stacknumber) {
        if ($scope.state === "PlayerTurnFinished") {
            var selectedCards = $scope.getSelectedCards();
            if (selectedCards.length == 1) {
                $http.get('/json/addToPhase/' + selectedCards[0] + "/" + stacknumber).success(function (data) {
                    $scope.update(data);
                });
            }
        } else if ($scope.state === "PlayerTurnNotFinished") {
            var selectedCards = $scope.getSelectedCards();
            if (selectedCards.length > 0) {
                var cardString = "";
                selectedCards.forEach(function (number) {
                    cardString += number + ";";
                });
                $http.get('/json/playPhase/' + cardString).success(function (data) {
                    $scope.update(data);
                });
            }
        }
    };

    $http.get('/json/update').success(function (data) {
        $scope.update(data);
    });
});

function debug(data) {
    console.log(data);
    return data;
}
