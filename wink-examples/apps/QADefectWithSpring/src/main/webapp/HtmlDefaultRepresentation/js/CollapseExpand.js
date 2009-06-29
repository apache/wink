
function CollapseExpand(id) {
  this.myId = id;
}

CollapseExpand.prototype.collapseExpand = function() {
  var elem = document.getElementById(this.myId);
  var imageElem = document.getElementById(this.myId + "_div");
  
  if(elem != null) {
    var display = elem.style.display;
    if(display != null && display != 'undefined' && display == 'none') {
      // the element is not visible, we need to expand
      elem.style.display = 'block';      
      imageElem.innerHTML = "-";
      return;
    }else{
      elem.style.display = 'none';
      imageElem.innerHTML = "+";
    }
  }
}

CollapseExpand.prototype.collapse = function() {
	var elementToCollapse = document.getElementById(this.myId);
	elementToCollapse.style.display = 'none';  
}
