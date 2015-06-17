$(document).ready(function(){  
    if (getCookie('openFilterItem') != null ) {
        var openwindows = getCookie('openFilterItem').toString().split('|');
        $(".filter_body").removeClass('openWindow');
        $(".filter_head").children("span").addClass('inactive');  
        $(".filter_head").children("span").removeClass('active'); 
        $.each(openwindows,function( intIndex, objValue ){
            if (objValue != "") {              
                var menubody = "#" + objValue;
                $(menubody).addClass('openWindow');
                $(menubody).prev(".filter_head").children("span").removeClass('inactive');
                $(menubody).prev(".filter_head").children("span").addClass('active');
            }
        });  
    } else {    	
        $(".filter_body").addClass('openWindow');
        $(".filter_head").children("span").removeClass('inactive');  
        $(".filter_head").children("span").addClass('active');       
    }   
    $(".filter_body").hide();
    $(".openWindow").show();
    $(".filter_head").click(function(){    	
        $(this).next(".filter_body").slideToggle(0);      
        if ($(this).next(".filter_body").hasClass('openWindow')) {
            $(this).next(".filter_body").removeClass('openWindow');
            $(this).children("span").removeClass('active'); 
            $(this).children("span").addClass('inactive');
        }
        else
        {
            $(this).next(".filter_body").addClass('openWindow');
            $(this).children("span").removeClass('inactive'); 
            $(this).children("span").addClass('active');
        }
        var openwindowsStr = "";            
        elements = $('.openWindow');
        elements.each(function() { openwindowsStr = openwindowsStr + "|" + $(this).attr('id').toString(); });
        setCookie("openFilterItem", openwindowsStr);        
    });
});
