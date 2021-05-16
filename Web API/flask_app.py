from PIL import Image
import base64
import io
from flask import Flask, request, jsonify
import json
from darknet_flask import initPredictor, performPredict, detections2json
from pathlib import Path

# pass __name__ and instantiate a Flask object
app = Flask(__name__)

# read Flask configuration
with open('./backend/flask_config.json','r',encoding='utf8')as fp:
    opt = json.load(fp)
    print('Flask Config : ',opt)

# initialize object detector: load the model and config files
initPredictor(configPath=opt['cfg'], weightPath=opt['weights'], metaPath=opt['meta'])

@app.route('/predict/', methods=['POST'])
# response detection of POST information
def get_prediction():
    response = request.get_json()
    data_str = response['image']
    point = data_str.find(',')
    base64_str = data_str[point:]  # remove unused part like this: "data:image/jpeg;base64,"
    image = base64.b64decode(base64_str) # base64 image decoding
    img = Image.open(io.BytesIO(image)) # open file
    if (img.mode != 'RGB'):
        img = img.convert("RGB")
    save_path = str(Path(opt['source']) / Path("img4predict.jpg")) # save image for prediction
    img.save(save_path)
    #img.save("./frontend/static/images/img4predict.jpg")

    # predicted image
    out_path = str(Path(opt['output']) / Path("img_predicted.jpg")) # save predicted image
    detections = performPredict(imagePath=save_path, thresh=opt['thresh'], outPath=out_path)
    #detections = performPredict(imagePath="./frontend/static/images/img4predict.jpg", thresh=opt['thresh'], outPath="./frontend/static/output/img_predicted.jpg")
    results = detections2json(detections)
    return jsonify(results)

@app.after_request
def add_headers(response):
    # 允许跨域
    response.headers.add('Access-Control-Allow-Origin', '*')
    response.headers.add('Access-Control-Allow-Headers', 'Content-Type,Authorization')
    return response

if __name__ == '__main__':
    #test_prediction()
    #app.run(debug=True, host='127.0.0.1')
    app.run(host='0.0.0.0')
    #app.run(host='127.0.0.1')
