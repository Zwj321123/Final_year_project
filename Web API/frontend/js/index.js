
const image = document.getElementById('image');
const canvas = document.getElementById('canvas');
const dropContainer = document.getElementById('container');
const warning = document.getElementById('warning');
const fileInput = document.getElementById('fileUploader');

// const URL = "http://127.0.0.1:5000/predict/"
// const URL = "http://192.168.16.121:5000/predict/"


// Get the URL of the server node
function GetUrl()
　　{
	var protocol = window.location.protocol.toString();
	var host =  document.domain.toString();
        var port = window.location.port.toString();
	var url = protocol + '//' + host + ":5000/predict/";
	return url;
　　}


const URL = GetUrl()
// alert(URL);

// Cancel event's default action and propagation
function preventDefaults(e) {
  e.preventDefault() //cancel event's default action
  e.stopPropagation() //stop propogation; stop the event being distributed to other Document nodes
};


// send image to the server and receive detection results and plot it on canvas
function communicate(img_base64_url) {
//applying AJAX
  $.ajax({
    url: URL,
    type: "POST",
    contentType: "application/json",
    data: JSON.stringify({"image": img_base64_url}), //base64 encoding
    dataType: "json"
  }).done(function(response_data) {
      drawResult(response_data.results);
  });
}

// address image uploaded by users and send it to the server to plot detection results.
function parseFiles(files) {
  const file = files[0];
  const imageType = /image.*/;
  if (file.type.match(imageType)) {
    warning.innerHTML = '';
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onloadend = () => {
      image.src = reader.result;
      // send the img to server
      communicate(reader.result);

    }
  } else {
    setup();
    warning.innerHTML = 'Please upload an image file.';
  }

}


// callback of receiving upload file
function handleFiles() {
  parseFiles(fileInput.files);
}

// callback of upload button
function clickUploader() {
  fileInput.click();
}

// selecting colors of bounding boxes
function selectColor(index) {
  var colors = ["aqua", "blue", "fuchsia", "gray", "green", "lime", "maroon", "navy", "olive", "orange", "purple", "red", "silver", "teal", "white", "yellow", "black"];

  i = index % 18;
  var color = colors[i];
  return color;

}

// plotting deection results on the image
function drawResult(results)
{
    canvas.width = image.width;
    canvas.height = image.height;
    ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.drawImage(image, 0, 0);
    var num_of_Potato = 0;
    var num_of_SweetPotato = 0;
    var num_of_Eggplant = 0;
    var num_of_IpomoeaTriloba = 0;
		//define empty
    var classLists = [num_of_Potato, num_of_SweetPotato, num_of_Eggplant, num_of_IpomoeaTriloba];
    var classNames = ['potato', 'sweet_potato', 'eggplant', 'Ipomoea_triloba'];
    var index = 0;
    var totalClasses = new Array();
    //define bbox, classs names and confidence values
    for (bboxInfo of results)
    {
      bbox = bboxInfo['bbox'];
      class_name = bboxInfo['name'];
      score = bboxInfo['conf'];

      ctx.beginPath();
      ctx.lineWidth="4";

      if (totalClasses.includes(class_name) == false)
        {
           totalClasses[index] = class_name;
           index += 1;
        }
      //ctx.strokeStyle="red";
      //ctx.fillStyle="red";

        switch (class_name) {
            case classNames[0]:
                classLists[0]++;
                break;
            case classNames[1]:
                classLists[1]++;
                break;
            case classNames[2]:
                classLists[2]++;
                break;
            case classNames[3]:
                classLists[3]++;
                break;
        }

      var i = totalClasses.indexOf(class_name)   // index of class_name
      ctx.strokeStyle = selectColor(i);
      ctx.fillStyle = selectColor(i);

      ctx.rect(bbox[0], bbox[1], bbox[2] - bbox[0], bbox[3] - bbox[1]);
      ctx.stroke();

      ctx.font="20px Arial";

      let content = class_name + " " + parseFloat(score).toFixed(2);
      ctx.fillText(content, bbox[0], bbox[1] < 20 ? bbox[1] + 30 : bbox[1]-5);
    }
    //summarize objects
    for (i = 0; i < classNames.length; i++) {
        ctx.strokeStyle = "white";
        ctx.fillStyle = "black";
        ctx.font = "30px Arial";
        ctx.fillText(classNames[i] + ": " + classLists[i], 20, 35+ 35*i);
    }

}


// Initialization function
async function setup() {
  // Make a detection with the default image
  var canvasTmp = document.createElement("canvas");
  canvasTmp.width = image.width;
  canvasTmp.height = image.height;
  var ctx = canvasTmp.getContext("2d");
  ctx.drawImage(image, 0, 0);
  var dataURL = canvasTmp.toDataURL("image/png");
  communicate(dataURL)
}

setup();
