from flask import Blueprint, jsonify, request
from api.service.tasks import sentiment_predict
from api.service.auth_service import token_required, check_whitelist
import json

api = Blueprint("api", __name__, url_prefix="/api")

@api.route('/analysis', methods = ['POST'])
@check_whitelist
@token_required
def analysis():
    params = json.loads(request.get_data())
    if len(params) == 0:
        return 'No parameter'
    contents = params['contents']
    result = sentiment_predict.delay(contents)
    return jsonify(result.get())