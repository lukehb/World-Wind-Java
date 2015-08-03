/**
 * Created by Matthew on 7/27/2015.
 */

define(function(){

    var Hud = function (id, screenLoc, selectorToAppendTo) {
        var self = this;
        this.ParentNode = (selectorToAppendTo) ? $(selectorToAppendTo) : $('body');
        this.ID = id;
        console.log(this.ParentNode)
        this.StyleSheet =
            $("<style>")
            .prop("type", "text/css")
                .html("\ ." + id + " {\
                        font-family: 'Ubuntu', sans-serif;\
                        font-size:14px;\
                        font-weight:300;\
                        position: fixed;\
                        background-color: #e5e5e5;\
                        border-radius: 10px;\
                        z-index: 25;\
                        left:" + screenLoc[0] + "px;\
                        top:" + screenLoc[1] + "px;\
                        color:#555;\
                        padding:10px;\
                        margin:0 20px;\
                        overflow:hidden;\
                        height:130;\
                        width:260;\
                        float:left;\
                        border:1px solid #ccc;\
                        box-shadow:0 0 10px 0 #aaa;\
                        -webkit-transition: all 0.7s ease-in-out;\
                        -moz-transition: all 0.7s ease-in-out;\
                        -ms-transition: all 0.7s ease-in-out;\
                        -o-transition: all 0.7s ease-in-out;\
                        transition: all 0.7s ease-in-out;\
                    }\
                    ." + id + "hover{\
                        float:left;\
                        background:#fff;\
                        border:1px solid #aaa;\
                        box-shadow:0 0 10px 0 #777;\
                    }\
                    ." + id + " h3 {\
                        text-align:center;\
                        padding:0px;\
                        border-bottom:1px solid #ccc;\
                    }\
                    ." + id + "info > p {\
                        text-align:center;\
                        padding:0 0 10px 0;\
                    }\
                    .read {\
                        text-align:center;\
                        font-size:16px;\
                        font-weight:800;\
                        color:#555;\
                        background:#444;\
                        padding:5px;\
                        border:1px solid #999;\
                        -webkit-border-radius: 5px 5px 5px 5px;\
                        border-radius: 5px 5px 5px 5px;\
                    }\
                    .read a {\
                        text-align:center;\
                        font-size:16px;\
                        font-weight:800;\
                        color:#fff;\
                        text-decoration:none;\
                    }\
                    .read:hover{\
                        background:#0099ff;\
                        text-align:center;\
                        font-size:16px;\
                        font-weight:800;\
                        color:#fff;\
                        text-decoration:none;\
                    }"
        );

        this.StyleSheet.appendTo("head");

        this.buildDiv();
        //this.assembleDisplay();

        return this
    };

    Hud.prototype.buildDiv = function () {
        var self = this;
        this.DIV = $('<div>');
        this.DIV.attr('class',this.ID);
        this.DIV.css('background-color','white');
        this.closeButtonAndTitleDiv = $('<h3>');
        this.closeButtonAndTitleDiv.append(this.ID);
        this.closeButton = $('<img>');
        this.closeButton.attr('src','img/load.png');
        this.closeButton.attr('width','20');
        this.closeButton.attr('height','20');
        this.closeButton.attr('alt','Pin');
        this.closeButton.attr('longdesc','img/load.png');
        this.closeButton.on('click', function (ev) {
            self.DIV.remove();
            self.StyleSheet.remove();
        });
        this.closeButtonAndTitleDiv.append(this.closeButton);
        this.DIV.append(this.closeButtonAndTitleDiv);
        this.ParentNode.append(this.DIV)

    };

    Hud.prototype.assembleDisplay = function (bodyText, buttonText, buttonFunction){
        if (!this.DIV){
            this.buildDiv()
        }

        this.boxText = $('<p>');
        this.boxText.append(bodyText);
        this.buttons = $('<div>');
        this.buttons.attr('class','read');
        this.buttonClick = $('<a>');
        this.buttonClick.attr('href','#');
        this.buttonClick.append(buttonText);
        this.buttonClick.on('click', buttonFunction)
        this.buttons.append(this.buttonClick);
        this.DIV.append(this.boxText);
        this.DIV.append(this.buttons);

    };

    Hud.prototype.addAnchor = function (anchor) {
        console.log('anchor ', anchor)
        this.DIV.append(anchor)
    };

    Hud.prototype.addCloseEvent = function (callback) {
        this.closeButton.on('click', callback)
    };


    return Hud
});