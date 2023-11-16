from functools import wraps
from flask import request, jsonify, current_app
from api.service.auth_service import authKey;
import datetime
import jwt

# decorator 함수
def check_whitelist(f):
    @wraps(f)
    def decorated_function(*args, **kwagrs):
        clientIp = request.environ.get('HTTP_X_REAL_IP', request.remote_addr)

        if(clientIp not in current_app.config['WHITE_LIST']):
            print(clientIp +": 접근 거부")
            return jsonify("access denied")

        print(clientIp + ": 접근 허가")
        return f(*args, **kwagrs)
    
    return decorated_function

# decorator 함수
def token_required(f):
    @wraps(f)
    def decorated_function(*args, **kwagrs):
        token = request.headers.get("Authorization")
        if(token is None):
            print("토큰이 유효하지 않음 : 접근 거부")
            return jsonify("Token is invalid : access denied")
        
        payload = jwt.decode(token, current_app.config['JWT_SECRET_KEY'], current_app.config['ALGORITHM'])

        if('authKey' not in locals() and 'authKey' not in globals()):
            print("토큰이 생성되지 않음 : 접근 거부")
            return jsonify("Token not created : access denied")
        
        if(payload['key'] != authKey.key):
            print("토큰이 유효하지 않음 : 접근 거부")
            return jsonify("Token is invalid : access denied")

        if(datetime.datetime.fromtimestamp(payload['exp']) < datetime.datetime.utcnow()):
            print("토큰이 만료됨 : 접근 거부")
            return jsonify("Token expired : access denied")
    

        print(" 토큰 유효함 : 접근 허가")

        return f(*args, **kwagrs)
    
    return decorated_function