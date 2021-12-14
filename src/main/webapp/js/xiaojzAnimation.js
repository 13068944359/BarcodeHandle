/**
 * 
 */

var xiaojzAni = {
	/**
	 * targetEleSelector 目标元素选择表达式（字符串）
	 * targetAttr 目标元素的目标参数类型（字符串）
	 * targetValue 目标元素的目标参数值（数值）
	 * timeInterval 动画间隔（数值）
	 * 使用方式：xiaojzAni.startAni("#testDiv","height",700,10);
	 */
	startElasticAni : function(targetEleSelector,targetAttr,targetValue,timeInterval){
		//clearInterval(timeIntervalAni);//防止重复触发

		var targetSpeed = 0;//动态速度
		var accelerateSpeed = 5;//加速度 单位用基础单位

		var timeIntervalAni = setInterval(function(){
			var sourceAttrStr = $(targetEleSelector).css(targetAttr);
			var sourceAttrVal = Number(sourceAttrStr.match(/\d+/)[0]);//元素原值
			var sourceAttrUnit = sourceAttrStr.replace(sourceAttrStr.match(/\d+/)[0],"");//元素原值单位

			var targetDistance = targetValue - sourceAttrVal;
			if(targetDistance > 0){
				targetSpeed += accelerateSpeed;
			}else{
				targetSpeed -= accelerateSpeed;
			}
			
			targetSpeed -= Math.ceil(targetSpeed/(2*accelerateSpeed));
			/*if(targetSpeed > 0){
				targetSpeed -= 1;
			}else{
				targetSpeed += 1;
			}*/
			//targetSpeed *= 0.9;//速度必须收敛,用乘法方式收敛速度过快，且数值容易异常

			if( Math.abs(targetSpeed) < accelerateSpeed && Math.abs(targetDistance) < accelerateSpeed){
				clearInterval(timeIntervalAni);
				$(targetEleSelector).css(targetAttr,targetValue + sourceAttrUnit);//手动设置到目标值
			}else{
				$(targetEleSelector).css(targetAttr,(sourceAttrVal + targetSpeed) + sourceAttrUnit);
			}
			//console.info("targetValue=" + targetValue);
			//console.info("sourceAttrVal=" + sourceAttrVal);
			//console.info("targetDistance=" + targetDistance);
			//console.info(targetSpeed);
		},timeInterval);
	},
	// 减速运动
	startSlowAni : function(targetEleSelector,targetAttr,targetValue,timeInterval){   
		//clearInterval(timeIntervalAni);//防止重复触发 
		var timeIntervalAni = setInterval(function(){
			var sourceAttrStr = $(targetEleSelector).css(targetAttr);
			var sourceAttrVal = Number(sourceAttrStr.match(/\d+/)[0]);//元素原值
			var sourceAttrUnit = sourceAttrStr.replace(sourceAttrStr.match(/\d+/)[0],"");//元素原值单位
			targetDistance = targetValue - sourceAttrVal;
			targetSpeed = targetDistance / 5;  //速度用百分比来算更好控制（无法确定大小的情况下）
			targetSpeed *= 0.9; //速度修正参数

			var absOfSpeed = Math.abs(targetSpeed);
			if(absOfSpeed<1 && targetDistance==0){
				clearInterval(timeIntervalAni);
			}else{
				// console.info(absOfSpeed<2);
				$(targetEleSelector).css(targetAttr,(sourceAttrVal + targetSpeed) + "px");
			}
			//console.info(targetSpeed);
		},timeInterval);
	}
}
