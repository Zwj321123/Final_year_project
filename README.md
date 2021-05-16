# Final_year_project
## Introduction
This work is about a mobile  machine learning application in detecting flowers of bioenergy crops.
A custom dataset containing 495 manually-labelled images is constructed for training and testing, and the latest state-of-the-art object detection models, YOLOv4 and YOLOv4-tiny, are selected as the flower detection models.
Some other milestone object detection models including YOLOv3, YOLOv3-tiny, SSD and Faster-RCNN are chosen as benchmarks for performance comparison. 
The comparative experiment results indicate that the retrained YOLOv4 model achieves a considerable high mean average precision (mAP = 91%) but a slower inference speed (FPS) on a mobile device, while the retrained YOLOv4-tiny has a lower mAP of 87% but reach a higher FPS of 9 on a mobile device. Two mobile applications are then developed by directly deploying YOLOv4-tiny model on a mobile app and by deploying YOLOv4 on a web API, respectively. 
The testing experiments indicate that both applications can not only achieve real-time and accurate detection, but also reduce computation burdens on mobile devices. 
## Training the Models
The YOLO serial models are trained on Google Colab. The Colab notebook can be found in ./Training/YOLO_training_and_testing.ipynb
### Configuring the Models
Before training, you should make your own obj.data and obj.name files. The obj.data and obj.name of my project are in ./Models/YOLO_series/config_files/
Other files including .cfg and .weights can be found here: https://github.com/AlexeyAB/darknet
### The loss and mAP curves of the YOLOv4(_left_) and YOLOv4-tiny(_right_) during the training process.
![loss](https://user-images.githubusercontent.com/50050000/118405901-cb1a7300-b671-11eb-98c1-562a2e4eae88.png)
### The P-R curves of the retrained YOLOv4(_left_) and YOLOv4-tiny(_right_) models.
![PR-curve-full](https://user-images.githubusercontent.com/50050000/118406237-184b1480-b673-11eb-9dd7-8e292ecc50d2.png)

## Flower Detection Mobile APP
The flower detection Mobie App should works under the Android 10+ environment. The retrained YOLOv4-tiny model is directedly deployed on the app (the mobile device). It achieves real-time detections with an average inference speed of 110 millisecond per frame.
The source code of the mobile app is in the Mobile App folder and it should run in Android Studio IDE with OpenCV SDK installed.
### The main user interface of the Flower Detection Mobile APP
![app](https://user-images.githubusercontent.com/50050000/118405532-47ac5200-b670-11eb-8980-f0ecdb63f604.jpg)
## Flower Detection Web API
The flower detection Web API is constructed based on FLASK framework and YOLOv4 dynamic-link library (dll)
The overall architecture of the API is as follow:
![API](https://user-images.githubusercontent.com/50050000/118406362-a626ff80-b673-11eb-8e22-384cd20562b9.png)
It can be divided into 2 portions: frontend and backend.
### Frontend:
The frontend is built by HTML, CSS and Javascript. Source codes can be found in: ./Web API/frontend
#### The main interface of the web page is as follow:
![微信图片_20210514141805](https://user-images.githubusercontent.com/50050000/118406335-90193f00-b673-11eb-9269-be48742e25c4.jpg)
### Backend:
The backend is built based on FLASK server and YOLO DLL. The YOLO DLL can be compiled in the Darknet Framework.
The code of the FLASK server is in: ./Web API/flask_app.py

