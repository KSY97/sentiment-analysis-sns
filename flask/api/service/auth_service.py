from functools import wraps
from flask import request, jsonify, current_app
import datetime
import jwt
import uuid


class AuthKey():
    def __init__(self):
        self.key = uuid.uuid4().hex

def generate_auth_key():
    global authKey
    authKey = AuthKey()
    return authKey

def generate_jwt_token(key):

    payload = {
         'key': key,
         'exp': datetime.datetime.utcnow() + datetime.timedelta(seconds=60 * 60 * 24)  # 로그인 24시간 유지
        }
    
    token = jwt.encode(payload, current_app.config['JWT_SECRET_KEY'], current_app.config['ALGORITHM'])

    return token

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
        print(token)
        if(token is None):
            print("토큰이 유효하지 않음 : 접근 거부")
            return jsonify("Token is invalid : access denied")
        
        payload = jwt.decode(token, current_app.config['JWT_SECRET_KEY'], current_app.config['ALGORITHM'])

        print(payload)

        print("1")
        if('authKey' not in locals() and 'authKey' not in globals()):
            print("토큰이 생성되지 않음 : 접근 거부")
            return jsonify("Token not created : access denied")
        
        print("2")
        if(payload['key'] != authKey.key):
            print("토큰이 유효하지 않음 : 접근 거부")
            return jsonify("Token is invalid : access denied")

        if(datetime.datetime.fromtimestamp(payload['exp']) < datetime.datetime.utcnow()):
            print("토큰이 만료됨 : 접근 거부")
            return jsonify("Token expired : access denied")
    

        print(" 토큰 유효함 : 접근 허가")

        return f(*args, **kwagrs)
    
    return decorated_function