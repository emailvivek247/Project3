$(function() {
	var curPanel = 3;
	var totalPanels			= $("#scrollContainert2").children().size();
		
	var regWidth			= $("#scrollContainert2 .panel").css("width");
	var regImgWidth			= $("#scrollContainert2 .panel img").css("width");
	var regTitleSize		= $("#scrollContainert2 .panel h2").css("font-size");
	var regParSize			= $("#scrollContainert2 .panel p").css("font-size");
	
	var movingDistance	    = 630;
	
	var curWidth			= 220;
	var curImgWidth			= 200;
	var curTitleSize		= "20px";
	var curParSize			= "15px";

	var $panels				= $('#slidert2 #scrollContainert2 > div');
	var $container			= $('#slidert2 #scrollContainert2');
	var $scroll1			= $('#slidert1 .scroll');
	var $scroll2			= $('#slidert2 .scroll');

	$panels.css({'float' : 'left','position' : 'relative'});
    
	$("#slidert2").data("currentlyMoving", false);

	$container
		.css('width', ((300 * $panels.length)) + 100 )
		.css('left', "-0px");
    
	$scroll1
	.css('width', ($("#slidert1").width() * 1));
	
	$scroll2
	.css('width', $("#slidert1").width());
		
	var scroll = $('#slidert2 #scrollt2').css('overflow', 'hidden');

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
        if (($("#slidert2").data("currentlyMoving") == false)) {
            
			$("#slidert2").data("currentlyMoving", true);
			
			var next         = direction ? curPanel + 1 : curPanel - 1;
			var leftValue    = $("#scrollContainert2").css("left");
			var movement	 = direction ? parseFloat(leftValue, 10) - movingDistance : parseFloat(leftValue, 100) + movingDistance;
		
			$("#scrollContainert2")
				.stop()
				.animate({
					"left": movement
				}, function() {
					$("#slidert2").data("currentlyMoving", false);
				});
			
			returnToNormal("#scrollContainert2 #panel_"+curPanel);
			growBigger("#scrollContainert2 #panel_"+next);
			
			curPanel = next;
			
			//remove all previous bound functions
			$("#scrollContainert2 #panel_"+(curPanel+1)).unbind();	
			
			//go forward
			$("#scrollContainert2 #panel_"+(curPanel+1)).click(function(){ change(true); });
			
            //remove all previous bound functions															
			$("#scrollContainert2 #panel_"+(curPanel-1)).unbind();
			
			//go back
			$("#scrollContainert2 #panel_"+(curPanel-1)).click(function(){ change(false); }); 
			
			//remove all previous bound functions
			$("#scrollContainert2 #panel_"+curPanel).unbind();			
		}
	}
	
	// Set up "Current" panel and next and prev
	growBigger("#scrollContainert2 #panel_3");		
	//$("#panel_"+(curPanel+1)).click(function(){ change(true); });
	//$("#panel_"+(curPanel-1)).click(function(){ change(false); });
	
	//when the left/right arrows are clicked
	$("#rightt2").click(function(){ change(true); });	
	$("#leftt2").click(function(){ change(false); });
	
	$(window).keydown(function(event){
	  switch (event.keyCode) {
			case 13: //enter
				$("#rightt2").click();
				break;
			case 32: //space
				$("#rightt2").click();
				break;
	    case 37: //left arrow
				$("#leftt2").click();
				break;
			case 39: //right arrow
				$("#rightt2").click();
				break;
	  }
	});
	
	jQuery(function($) {
		   $('div#wrapper2')
	        .bind('mousewheel', function(event, delta) {
	        	var dir = delta > 0 ? 'Up' : 'Down',
	                vel = Math.abs(delta);
	        	
	        	if (dir == "Down") {
	            	change(true);
	            }
	            else if (dir == "Up") {
	            	change(false);
	            }
	            return false;
	        });
	});
});