/**
 * Created by tabuechn on 25.11.2015.
 */
var socket;
var playerCards = [["1", "Blue"], ["1", "Blue"], ["1", "Blue"], ["1", "Blue"], ["1", "Blue"], ["1", "Blue"], ["1", "Blue"], ["1", "Blue"], ["1", "Blue"], ["1", "Blue"]];
var opponentsCards = 10;

var discardCard = null;

var stack1 = [['1', 'Yellow'], ['1', 'Yellow']]

var stack2 = null;

var stack3 = null;

var stack4 = null;

var appURL = "phasex.herokuapp.com";
//var appURL = "localhost:9000";

$(function () {
    updateGameField();
})


function createCard(cardNumber, cardColor, className) {
    if (!(cardNumber === null || cardColor === null)) {
        var cardElement = new Image;
        cardElement.className = className;
        cardElement.src = "assets/images/Card/Card" + cardNumber + ".png";
        cardElement.style.backgroundImage = "url('assets/images/Card/Card" + cardColor + ".png')";
        return cardElement;
    } else if (cardNumber === "Back" || cardColor === "Back") {
        var cardElement = new Image;
        cardElement.className = className;
        //cardElement.src = "assets/images/Card/CardBACK.png";
        cardElement.style.backgroundImage = "url('assets/images/Card/CardBACK.png')";
        return cardElement;
    } else {
        var cardElement = new Image;
        cardElement.className = className;
        cardElement.src = "assets/images/Card/CardNO.png";
        return cardElement;
    }

}

function fillStack(stack) {
    var stackID;
    var stackarray = null;
    switch (stack) {
        case "stack1":
            stackID = "#stack1";
            stackarray = stack1;
            break;
        case "stack2":
            stackID = "#stack2";
            stackarray = stack2;
            break;
        case "stack3":
            stackID = "#stack3";
            stackarray = stack3;
            break;
        default:
            stackID = "#stack4";
            stackarray = stack4;
    }
    //stackID += " .stackCards";
    if (stackarray === null) {
        var emptyCard = new Image;
        emptyCard.src = "/assets/images/Card/CardNO.png";
        emptyCard.className = "StackCard"
        $(stackID).append(emptyCard);
    } else {
        var cards = $(stackID);
        stackarray.forEach(function (entry) {
            cards.append(createCard(entry[0], entry[1], "StackCard"));
        });
    }
}

function createHand(hand) {
    var handID;
    var images;
    if (hand === "player") {
        handID = "#playerCardsContainer";
        images = "#playerCardsContainer img";
        playerCards.forEach(function (entry) {
            $(handID).append(createCard(entry[0], entry[1], "Card"));

        });
    } else {
        handID = "#opponentCardsContainer";
        images = "#opponentCardsContainer img";
        for (i = 0; i < opponentsCards; i += 1) {
            $(handID).append(createCard("Back", "Back", "Card"));
        }
    }

    var allbutfirst = $(images);
    for (i = 0; i < allbutfirst.length; i += 1) {
        switch (i) {
            case 0:
                allbutfirst[i].className = "firstCard Card";
                break;
            case 1:
                allbutfirst[i].className = "secondCard Card";
                break;
            case 2:
                allbutfirst[i].className = "thirdCard Card";
                break;
            case 3:
                allbutfirst[i].className = "fourthCard Card";
                break;
            case 4:
                allbutfirst[i].className = "fifthCard Card";
                break;
            case 5:
                allbutfirst[i].className = "sixthCard Card";
                break;
            case 6:
                allbutfirst[i].className = "seventhCard Card";
                break;
            case 7:
                allbutfirst[i].className = "eighthCard Card";
                break;
            case 8:
                allbutfirst[i].className = "ninthCard Card";
                break;
            case 9:
                allbutfirst[i].className = "tenthCard Card";
                break;
            default:
                allbutfirst[i].className = "eleventhCard Card";
        }
    }
}

function updateGameField() {


    createHand("player");
    createHand("opponent");


    $("#playerCardsContainer img").each(function () {
        $(this).click(function () {

            if ($(this).hasClass("CardUp")) {
                $(this).removeClass("CardUp");
            } else {
                $(this).addClass("CardUp");
            }
        });
    });

    $('#discardPile').append(createCard(null, null, "PileCard"));


    fillStack("stack1");
    fillStack("stack2");
    fillStack("stack3");
    fillStack("stack4");
}

(function connect() {
    socket = new WebSocket("ws://" + appURL + "/socket");

    //message('Socket Status: ' + socket.readyState + ' (ready)');


    socket.onopen = function () {
        console.log('Socket Status: ' + socket.readyState + ' (open)');
        socket.send("DISCARD");
    };

    socket.onmessage = function (msg) {
        console.log(msg);
        var data = JSON.parse(msg.data);
        console.log(data);
        //changeState(data);
    };

    socket.onclose = function () {
        console.log('Socket Status: ' + socket.readyState + ' (Closed)');
    };


})();