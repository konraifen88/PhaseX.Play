/**
 * Created by tabuechn on 25.11.2015.
 */
var socket;
var playerCards = [["ONE", "BLUE"], ["ONE", "BLUE"], ["ONE", "BLUE"], ["ONE", "BLUE"],["ONE", "BLUE"], ["ONE", "BLUE"],["ONE", "BLUE"], ["ONE", "BLUE"],["ONE", "BLUE"], ["ONE", "BLUE"],["ONE", "BLUE"]];
var opponentsCards = 10;

var discardCard = null;
var discardIsEmtpy = false;

var roundState = "StartPhase";

var stack1 = [['ONE', 'YELLOW'], ['ONE', 'YELLOW']];

var stack2 = null;

var stack3 = null;

var stack4 = null;

var appURL = "phasex.herokuapp.com";
//var appURL = "localhost:9000";

var userID;


$(function () {
    userID = $("#UID").text();
    connect();
});


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
    if (stackarray === null || stackarray.length === 0) {
        var emptyCard = new Image;
        emptyCard.src = "/assets/images/Card/CardNO.png";
        emptyCard.className = "StackCard";
        $(stackID).append(emptyCard);
    } else {
        var cards = $(stackID);
        for(var cardCounter = 0; cardCounter < stackarray.length; cardCounter++) {
            var entry = stackarray[cardCounter];
            cards.append(createCard(entry.number, entry.color, "StackCard"));
        }
    }
}


function createHand(hand) {
    var handID;
    var images;
    var i;
    if (hand === "player") {
        handID = "#playerCardsContainer";
        images = "#playerCardsContainer img";
        $(handID).empty();
        for(i=0; i < playerCards.length; i += 1) {
            var entry = playerCards[i];
            $(handID).append(createCard(entry.number, entry.color, "Card"));

        }
    } else {
        handID = "#opponentCardsContainer";
        images = "#opponentCardsContainer img";
        $(handID).empty();
        for (i = 0; i < opponentsCards; i += 1) {
            $(handID).append(createCard("BACK", "BACK", "Card"));
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

function getSelectedCards() {
    var allCards = $("#playerCardsContainer").find("img");
    var returnString = "";
    for(var cardCounter = 0; cardCounter < allCards.length; cardCounter += 1) {
        var classes = allCards[cardCounter].className.split(" ");
        for (var classCounter = 0; classCounter < classes.length; classCounter += 1) {
            if (classes[classCounter] === "CardUp") {
                returnString += (" " + cardCounter);
            }
        }
    }
    return returnString;
}

function updateGameField() {


    createHand("player");
    createHand("opponent");


    $("#playerCardsContainer").find("img").each(function () {
        $(this).click(function () {

            if ($(this).hasClass("CardUp")) {
                $(this).removeClass("CardUp");
            } else {
                $(this).addClass("CardUp");
            }
        });
    });

    var discardElement = $("#discardPile");

    discardElement.empty();
    discardElement.off();
    if(discardIsEmtpy) {
        discardElement.append(createCard(null, null, "PileCard"));
    } else {
        discardElement.append(createCard(discardCard.number, discardCard.color, "PileCard"));
    }
    discardElement.click(discardClick);


    for(var stackCounter = 1; stackCounter <= 4; stackCounter += 1) {
        var stackString = "#stack" + stackCounter;
        $(stackString).empty();
        fillStack(stackString.substring(1,stackString.length));
    }
}

function discardClick() {
    if(roundState == "DrawPhase") {
        socket.send("drawOpen");
    }
    if(roundState == "PlayerTurnFinished" || roundState == "PlayerTurnNotFinished") {
        var selectedCards = "discard" + getSelectedCards();
        if (selectedCards.split(" ").length == 2) {
            socket.send(selectedCards);
        } else {
            alert("You cannot discard more than one Card");
        }
    }
}

function stackClick(stackNumber) {
    if(roundState === "PlayerTurnNotFinished") {
        var selected = "playPhase" + getSelectedCards();
        if(selected.length > 9) {
            socket.send(selected);
        }
    } if(roundState === "PlayerTurnFinished") {
        var selected = "addToPhase " + stackNumber + getSelectedCards();
        if(selected.split(" ").length != 3 ) {
            alert("You can only add 1 Card to a Phase");
        } else {
            socket.send(selected);
        }
    }
}

function drawHidden() {
    if(roundState == "DrawPhase") {
        socket.send("drawHidden");
    }
}

function connect() {
    var origin = window.location.origin.replace("http","ws");
    socket = new WebSocket(origin + "/singlePlayer/socket/" + userID);

    socket.onopen = function () {
        console.log('Socket Status: ' + socket.readyState + ' (open)');
        socket.send("update");
    };

    socket.onmessage = function (msg) {
        if(msg.data != "stayingAlive") {
            console.log(msg.data);
            var data = JSON.parse(msg.data);
            console.log(data);
            playerCards = data.map.playerHand;
            opponentsCards = data.map.opponent.length;
            stack1 = data.map.stack1;
            stack2 = data.map.stack2;
            stack3 = data.map.stack3;
            stack4 = data.map.stack4;
            roundState = data.map.roundState;
            discardIsEmtpy = data.map.discardIsEmpty;
            discardCard = data.map.discard[data.map.discard.length - 1];
            updateGameField();
        } else {
            console.log("stayingAlive");
        }
    };

    socket.onclose = function () {
        console.log('Socket Status: ' + socket.readyState + ' (Closed)');
    };

    console.log("Socket Init done");

}