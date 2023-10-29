from flask import Blueprint, jsonify, request
from api import sentiment_analysis_service
import json

api = Blueprint("api", __name__, url_prefix="/api")

@api.route('/home')
def home():
    return 'This is api home!'

@api.route('/tospring', methods = ['POST'])
def spring():
    params = json.loads(request.get_data())
    if len(params) == 0:
        return 'No parameter'
    contents = params['contents']
    predict = sentiment_analysis_service.sentiment_predict(contents)
    return jsonify(predict)