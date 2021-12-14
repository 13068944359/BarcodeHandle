/**
 * 
 */


function initUploadElement(elementSelector){
	var d = new Date();
	var fileUploadInputId = "upload" + d.getHours() + d.getHours() + d.getSeconds();// generate a random name
	var fileUploadEmementHtml = '<input type="file" id="' + fileUploadInputId + '"  style="display:none;" >';  
	$("body").append(fileUploadEmementHtml);
	
	$(elementSelector).click(function(){
		$("#" + fileUploadInputId).click();
	});
	
	return fileUploadInputId;//return element id 
}
