/**
 * If everything works right this class was
 * created by Konraifen88 on 14.01.2016.
 * If it doesn't work I don't know who the hell wrote it.
 */

$(function () {
    if ((window.location.pathname === "/")) {
        $(backButton).hide()
    }
    if ((window.location.pathname.search("\/chat\/+") == -1)) {
        $(disconnect).hide()
    }
    if ($("#playerName").text() == "") {
        $(profileInformation).hide()
        $(logout).hide()
    }
});

