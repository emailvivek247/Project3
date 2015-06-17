/**************************************************************************************
 * jQuery MsgBox 0.3.7
 * by composite (ukjinplant@msn.com)
 * http://blog.hazard.kr
 * This project licensed under a MIT License.
 **************************************************************************************/;
(function ($) {
    var nofix = $.browser.msie && ~~$.browser.version < 8,
        fixed = nofix ? 'absolute' : 'fixed';
    $.msgbox = function (msg, options) {
        //?? ??
        options = $.extend({}, $.msgbox.options, options);
        //?? ? ?? ??
        var io = {},
            mb = 'msgbox-',
            cok = mb + 'ok',
            cno = mb + 'no',
            pw = 'password',
            styles = options.css || {},
            t = !0,
            f = !1,
            p = ('input' in options),
            q = !! options.confirm,
            iae = function(e) { //???? ??? ????? ??? ?????? ?? ??? ??
                setTimeout(function(){
                    var act=$(document.activeElement),ms=['.'+mb+'input','.'+mb+'button'];
                    if(act.length&&(act.is(ms[0])||act.is(ms[1]))){console.log('good.');}
                    else $C.find(ms+'').eq(0).focus();
                },0);
            },
            $C = $("<div></div>").addClass(mb + 'ui').css(styles.ui || {}),
            //???
            $M = $("<div>&shy;</div>").addClass(mb + 'modal').css(styles.modal || {}),
            //??? ??
            $T = $("<pre></pre>").addClass(mb + 'msg').css(styles.msg || {}).html(msg).appendTo($C),
            //?? ??
            $I = p ?
                $("<div><input type='" + (options[pw] ? pw : 'text') + "'/></div>").addClass(mb + 'inbox').css(styles.indiv || {}).children()
                    .addClass(mb + 'input').css(styles.input || {}).bind('keydown',function(e){//?? ?? ? ??? ?? ???
                        if((window.event ? window.event.keyCode : e.which)==9&&e.shiftKey){
                            e.preventDefault();
                            $C.find('.'+mb+'button').filter(':last').focus();
                        }
                    }).bind('blur',iae).end().appendTo($C)
                : null,
            //?? ??? ???
            $B = $("<div></div>").addClass(mb + 'buttons').css(styles.buttons || {}).appendTo($C),
            //?? ?? ??
            $BT = $("<button></button>").addClass(mb + 'button').css(styles.button || {}).bind('keydown',function(e){
                if(this!=document.activeElement) return;
                e.stopPropagation();
                var code = window.event ? window.event.keyCode : e.which,that=$(this),target,shift=e.shiftKey;
                switch (code) {
                    case 9://?? ??? ?? ?? ? ??? ???
                    case 39://???? ??? ?? ????? ???
                        e.preventDefault();
                        if(target=that[code==9&&shift?'prev':'next']('button'),target.length) target.focus();
                        else if(code==9){
                            if(target=$C.find('.'+mb+'input'),target.length) target.select();
                            else if(target=that[shift?'next':'prev']('button'),target.length) target.focus();
                        }
                        break;
                    case 37://???? ?? ????? ???
                        e.preventDefault();
                        if(target=that.prev('button'),target.length) target.focus();
                        break;
                    case 27://ESC? ??? ????
                        e.preventDefault();
                        $C.find('button.' + (p || q ? cno : cok)).trigger('click');
                        break;
                }
            }).bind('blur',iae),
            //?? ??
            $BS = [
            $BT.clone(t).addClass(cok).text(q ? options.yes : options.ok).appendTo($B), p || q ? $BT.clone(t).addClass(cno).text(options.no).appendTo($B) : null]; //?? ???
        $C.add($M).bind('keydown',function(){});
        //?? ??? ????
        if (p) {
            options.confirm = t; //?? ?? ??.
            if (typeof (options.input) == 'string') $I.children().val(options.input);
        }
        //??? ???? ?
        io.before = function (e) {
            e.stopPropagation();
            var code = window.event ? window.event.keyCode : e.which;
            //?? ??????? before? ???? ??.
            if(e.target.type=='text'&&!code){
                $C.find('button.' + (p || q ? cno : cok)).trigger('click');
                return f;
            }
            switch (code) {
                case 13:
                    $C.find('button.' + cok).trigger('click');
                    return f;
                case 27:
                    $C.find('button.' + (p || q ? cno : cok)).trigger('click');
                    return f;
            }
        };
        //body? ?? ? ???? ??
        var kp = 'keypress',
            kt = '.' + mb + 'ui,.' + mb + 'modal',
            $D = $(document.documentElement ? document.documentElement : document.body).append($M).append($C).bind(kp, io.before);
        //??? ???? ?
        io.after = function (b, v) {
            for (var i = 0, cn = b.className.split(' '); i < cn.length; i++)
            switch (cn[i]) {
            case cok:
                switch (t) {
                case p:
                    options.submit.call($C[0], v);
                    break;
                case q:
                    options.submit.call($C[0], !! t);
                    break;
                default:
                    options.submit.call($C[0]);
                    break;
                }
                break;
            case cno:
                if (p || !(p && q)) {
                    options.submit.call($C[0]);
                } else {
                    options.submit.call($C[0], f);
                }
                break;
            }
            $D.unbind(kp, io.before);
        };
        //?? ?? ?? ? ??
        $C.delegate('button', 'click', function (e) {
            $C.add($M).remove();
            io.after(this, p ? $I.children().val() : null);
        });
        //???? ????
        if (styles.ui) $C.css({
            'margin-left': ~~ (-$C.outerWidth() * 0.5) + 'px',
            'margin-top': ~~ (-$C.outerHeight() * 0.32) + 'px'
        });
        //??? ???
        if (p) $C.find('input:text').select();
        else $C.find('button:eq(0)').focus();
        return $C;
    };
    $.extend($.msgbox, {
        strings: {
            ok: 'OK',
            yes: 'OK',
            no: 'Cancel'
        },
        css: {
            ui: {
                'border': '1px solid black',
                'font': '"Trebuchet MS", Arial, Helvetica, sans-serif;',
                'background-color': 'white',
                'position': fixed,
                'left': '50%',
                'top': '32%',
                'overflow': 'hidden',
                '-moz-border-radius': '7px',
                '-webkit-border-radius': '7px',
                'text-align': 'center'

            },
            modal: {
                'position': fixed,
                'left': '0',
                'top': '0',
                'right': '0',
                'bottom': '0',
                'background-color': 'black',
                'opacity': '.4'
            },
            msg: {
                'padding': '2em 4em',
                'overflow': 'hidden',
                'font-family': '"Trebuchet MS", Arial, Helvetica, sans-serif' //,'max-width':(screen.availWidth*0.9)+'px'
            },
            buttons: {
                'padding': '1em',
                'background-color': '#eee',
                'text-align': 'right',
                'overflow': 'hidden'
            },
            button: {
                'width': '72px',
                'margin': 'auto .25em'
            },
            indiv: {
                'width': '90%',
                'margin': '-2em auto 2em',
                'border': '0px inset #3D7BAD'
            },
            input: {
                'width': '99%',
                'display': 'none',
                'border': '0'
            }

        }
    });
    $.msgbox.options = {
        submit: function () {},
        confirm: false,
        //input:false,
        css: $.msgbox.css,
        ok: $.msgbox.strings.ok,
        yes: $.msgbox.strings.yes,
        no: $.msgbox.strings.no
    };
    $.alert = function (msg, callback) {
        return $.msgbox(msg, {
            submit: callback
        });
    };
    $.confirm = function (msg, callback) {
        return $.msgbox(msg, {
            confirm: true,
            submit: callback
        });
    };
    $.prompt = function (msg, val, callback, pw) {
        var shift = $.isFunction(val);
        return $.msgbox(msg, {
            input: shift ? true : val,
            submit: shift ? val : callback,
            password: shift ? callback : pw
        });
    };
})(jQuery);