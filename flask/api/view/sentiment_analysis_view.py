from flask import Blueprint, jsonify, request
from api.service.tasks import sentiment_predict
from api.service.auth_service import check_whitelist, token_required
import json

api = Blueprint("api", __name__, url_prefix="/api")

@api.route('/home')
def home():
    return 'This is api home!'

@api.route('/tospring', methods = ['POST'])
@token_required
def spring():
    params = json.loads(request.get_data())
    if len(params) == 0:
        return 'No parameter'
    contents = params['contents']
    result = sentiment_predict.delay(contents)
    return jsonify(result.get())