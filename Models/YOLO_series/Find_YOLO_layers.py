import cv2

def getOutputsNames(net):

    layersNames = net.getLayerNames()

    return [layersNames[i[0]-1] for i in net.getUnconnectedOutLayers()]

net = cv2.dnn.readNetFromDarknet("yolov3-tiny-flower.cfg","yolov3-tiny-flower_best.weights")

print(getOutputsNames(net))
