$(function() {
	var curPanel = 3;
	var totalPanels			= $("#scrollContainert1").children().size();
		
	var regWidth			= $("#scrollContainert1 .panel").css("width");
	var regImgWidth			= $("#scrollContainert1 .panel img").css("width");
	var regTitleSize		= $("#scrollContainert1 .panel h2").css("font-size");
	var regParSize			= $("#scrollContainert1 .panel p").css("font-size");
	
	var movingDistance	    = 630;
	
	var curWidth			= 220;
	var curImgWidth			= 200;
	var curTitleSize		= "20px";
	var curParSize			= "15px";

	var $panels				= $('#slidert1 #scrollContainert1 > div');
	var $container			= $('#slidert1 #scrollContainert1');
	
	$panels.css({'float' : 'left','position' : 'relative'});
    
	$("#slidert1").data("currentlyMoving", false);
	 
	$container
		.css('width', ((300 * $panels.length)) + 100 )
		.css('left', "-0px");
	
	var scroll = $('#slidert1 #scrollt1').css('overflow', 'hidden');

	function returnToNormal(element) {
		$(element)
			.animate({ width: regWidth })
			.find("img")
			.animate({ width: regImgWidth })
		    .end()
			.find("h2")
			.animate({ fontSize: regTitleSize })
			.end()
			.find("p")
			.animate({ fontSize: regParSize });
	};
	
	function growBigger(element) {
		$(element)
			.animate({ width: curWidth })
			.find("img")
			.animate({ width: curImgWidth })
		    .end()
			.find("h2")
			.animate({ fontSize: curTitleSize })
			.end()
			.find("p")
			.animate({ fontSize: curParSize });
	}
	
	//direction true = right, false = left
	function change(direction) {	   
	    //if not at the first or last panel		
	    if((direction && !(curPanel < totalPanels - 1)) || (!direction && (curPanel <= 2))) { return false; }	
        
        //if not currently moving
        if (($("#slidert1").data("currentlyMoving") == false)) {
            
			$("#slidert1").data("currentlyMoving", true);
			
			var next         = direction ? curPanel + 1 : curPanel - 1;
			var leftValue    = $("#scrollContainert1").css("left");
			var movement	 = direction ? parseFloat(leftValue, 10) - movingDistance : parseFloat(leftValue, 100) + movingDistance;
		
			$("#scrollContainert1")
				.stop()
				.animate({
					"left": movement
				}, function() {
					$("#slidert1").data("currentlyMoving", false);
				});
			
			returnToNormal("#scrollContainert1 #panel_"+curPanel);
			growBigger("#scrollContainert1 #panel_"+next);
			
			curPanel = next;
			
			//remove all previous bound functions
			$("#scrollContainert1 #panel_"+(curPanel+1)).unbind();	
			
			//go forward
			$("#scrollContainert1 #panel_"+(curPanel+1)).click(function(){ change(true); });
			
            //remove all previous bound functions															
			$("#scrollContainert1 #panel_"+(curPanel-1)).unbind();
			
			//go back
			$("#scrollContainert1 #panel_"+(curPanel-1)).click(function(){ change(false); }); 
			
			//remove all previous bound functions
			$("#scrollContainert1 #panel_"+curPanel).unbind();			
		}
	}
	
	// Set up "Current" panel and next and prev
	growBigger("#scrollContainert1 #panel_3");		
	//$("#panel_"+(curPanel+1)).click(function(){ change(true); });
	//$("#panel_"+(curPanel-1)).click(function(){ change(false); });
	
	//when the left/right arrows are clicked
	$("#rightt1").click(function(){ change(true); });	
	$("#leftt1").click(function(){ change(false); });
	
	$(window).keydown(function(event){
	  switch (event.keyCode) {
			case 13: //enter
				$("#leftt1").click();
				break;
			case 32: //space
				$("#rightt1").click();
				break;
	    case 37: //left arrow
				$("#leftt1").click();
				break;
			case 39: //right arrow
				$("#rightt1").click();
				break;
	  }
	});
	
	jQuery(function($) {
	    $('div#wrapper1')
	         .bind('mousewheel', function(event, delta) {
	        	 var dir = delta > 0 ? 'Up' : 'Down',
                 vel = Math.abs(delta);
	        	 
	        	if (dir == 'Down') {
	        		change(true);
	            }
	            else if (dir == 'Up') {
	            	change(false);
	            }
	            return false;
	        });
	});
});