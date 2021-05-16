# Final_year_project
## Introduction
This work is about a mobile  machine learning application in detecting flowers of bioenergy crops.
A custom dataset containing 495 manually-labelled images is constructed for training and testing, and the latest state-of-the-art object detection models, YOLOv4 and YOLOv4-tiny, are selected as the flower detection models.
Some other milestone object detection models including YOLOv3, YOLOv3-tiny, SSD and Faster-RCNN are chosen as benchmarks for performance comparison. 
The comparative experiment results indicate that the retrained YOLOv4 model achieves a considerable high mean average precision (mAP = 91%) but a slower inference speed (FPS) on a mobile device, while the retrained YOLOv4-tiny has a lower mAP of 87% but reach a higher FPS of 9 on a mobile device. Two mobile applications are then developed by directly deploying YOLOv4-tiny model on a mobile app and by deploying YOLOv4 on a web API, respectively. 
The testing experiments indicate that both applications can not only achieve real-time and accurate detection, but also reduce computation burdens on mobile devices. 

## Flower Detection Mobile APP
The source code of the mobile app is in the Mobile App folder.
It should run in Android Studio IDE with OpenCV SDK installed.
### The main user interface of the Flower Detection Mobile APP

