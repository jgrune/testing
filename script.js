angular.module("myPage", [])
  .controller("pageController", pageControllerCallback)

function pageControllerCallback(){
  console.log("angular is connected!")
}



// **********
//   timer
// **********

// let timer = setInterval(startTimer, 1000);
// let count = 0;
//
// function startTimer(){
//   var d = new Date();
//   var t = d.toLocaleTimeString();
//   console.log(t);
//   count ++
//   console.log(count)
//
//   if (count === 10){
//     clearInterval(timer);
//   }
// }
