from flask import Blueprint
from api.service.auth_service import check_whitelist, generate_auth_key, generate_jwt_token

auth = Blueprint("auth", __name__, url_prefix="/auth")

@auth.route('/home')
def home():
    return 'This is auth home!'

@auth.route('/token')
@check_whitelist
def token():
    authKey = generate_auth_key()
    key = authKey.key
    print(key)
    token = generate_jwt_token(key)
    return token