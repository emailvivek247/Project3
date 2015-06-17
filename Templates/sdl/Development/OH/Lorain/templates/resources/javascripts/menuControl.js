
$(document).ready(function(){  
    if ($.cookie('openMenuItem') == null ) {
        $.cookie("openMenuItem", "menu_body1");
        $(".menu_body").removeClass('openWindow');
        $("#menu_body1").addClass('openWindow');
        $("#menu_body1").prev(".menu_head").children("span").removeClass('inactive');
        $("#menu_body1").prev(".menu_head").children("span").addClass('active');
    }
    else if ($.cookie('openMenuItem').toString() == "all" ) {      
         $(".menu_body").addClass('openWindow');
         $(".menu_list .menu_head span").removeClass('inactive');
         $(".menu_list .menu_head span").addClass('active');
         $(".menu_body").show(); 
    }
    else if ($.cookie('openMenuItem').toString() == "none" ) {     
         $(".menu_body").removeClass('openWindow');
         $(".menu_list .menu_head span").removeClass('active');
         $(".menu_list .menu_head span").addClass('inactive');
         $(".menu_body").hide();     
    }
    else {      
        var openwindows = $.cookie('openMenuItem').toString().split('|');
        $(".menu_body").removeClass('openWindow');
        $.each(openwindows,function( intIndex, objValue ){
            if (objValue != "") {              
                var menubody = "#" + objValue;
                $(menubody).addClass('openWindow');
                $(menubody).prev(".menu_head").children("span").removeClass('inactive');
                $(menubody).prev(".menu_head").children("span").addClass('active');
            }
        });  
    }
    
    
    
    $(".menu_body").hide();
    $(".openWindow").show(); 
    
    $(".scaffold_body").hide();
    $(".openScaffoldWindow").show();
   
    
    $(".menu_head").click(function(){
        $(this).next(".menu_body").slideToggle(0);      
        if ($(this).next(".menu_body").attr('class').length  > 9) {
            $(this).next(".menu_body").removeClass('openWindow');
            $(this).children("span").removeClass('active'); 
            $(this).children("span").addClass('inactive');
        }
        else
        {
            $(this).next(".menu_body").addClass('openWindow');
            $(this).children("span").removeClass('inactive'); 
            $(this).children("span").addClass('active');
        }
        var openwindowsStr = "";            
        elements = $('.openWindow');
        elements.each(function() { openwindowsStr = openwindowsStr + "|" + $(this).attr('id').toString(); });
        $.cookie("openMenuItem", openwindowsStr);
    });
    
    
    $(".scaffold_head").click(function(){
        $(this).next(".scaffold_body").slideToggle(0);      
        if ($(this).next(".scaffold_body").attr('class').length  > 15) {
            $(this).next(".scaffold_body").removeClass('openScaffoldWindow');
            $(this).children("span").removeClass('active'); 
            $(this).children("span").addClass('inactive');
        }
        else
        {
            $(this).next(".scaffold_body").addClass('openScaffoldWindow'); 
            $(this).children("span").removeClass('inactive'); 
            $(this).children("span").addClass('active');
        }        
    });
    
    $(".expand_all").click(function(){
         $.cookie("openMenuItem", "all");
         $(".menu_body").addClass('openWindow');
         $(".menu_list .menu_head span").removeClass('inactive');
         $(".menu_list .menu_head span").addClass('active');
         $(".menu_body").show();
    });
    $(".collapse_all").click(function(){
         $.cookie("openMenuItem", "none");
         $(".menu_body").removeClass('openWindow'); 
         $(".menu_list .menu_head span").removeClass('active');
         $(".menu_list .menu_head span").addClass('inactive');
         $(".menu_body").hide();
    });
    
    $(".expand_pageStyleDesigner").click(function(){
        $.cookie("openMenuItem", "all");
        $(".scaffold_list .scaffold_body").addClass('openWindow'); 
        $(".scaffold_list .scaffold_head span").removeClass('inactive');
        $(".scaffold_list .scaffold_head span").addClass('active');  
        $(".scaffold_list .scaffold_body").show();
   });
   $(".collapse_pageStyleDesigner").click(function(){
        $.cookie("openMenuItem", "none");
        $(".scaffold_list .scaffold_body").removeClass('openWindow');  
        $(".scaffold_list .scaffold_head span").removeClass('active');
        $(".scaffold_list .scaffold_head span").addClass('inactive'); 
        $(".scaffold_list .scaffold_body").hide();
   });
   $("a.undermenu").click(function() {
		$.cookie("tab_select", null);
	});
});
