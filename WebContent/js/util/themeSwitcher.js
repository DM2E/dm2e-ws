define(
    [
        'jquery'
    ],
    function ($) {

        var currentState;
        var themes = {
            light : "css/theme-light.css",
            dark : "css/theme-dark.css",
        }
        var toggleState = function () {
            currentState = currentState == 'light' ? 'dark' : 'light';
        }
        var applyTheme = function() {
            $("*[data-theme]").remove();
            $("head").append(
                $("<link/>")
                    .attr("data-theme", "true")
                    .attr("rel", "stylesheet")
                    .attr("type", "text/css")
                    .attr("href", themes[currentState]));
        }
        var getTheme = function() {
            return currentState;
        }
        var setTheme = function(name) {
            currentState = name === 'dark' ? 'dark' : 'light';
            applyTheme();
        };
        var toggle = function () {
            toggleState();
            applyTheme();
        };
//        applyTheme();
        return {
            toggle: toggle,
            setTheme: setTheme,
            getTheme: getTheme,
        }
    });
