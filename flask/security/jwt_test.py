import jwt

JWT_SECRET_KEY = 'key'
algorithm = "HS256"
jwt_token = jwt.encode({'id' : "gg"}, JWT_SECRET_KEY, algorithm)
print(jwt_token)
print(jwt.decode(jwt_token, JWT_SECRET_KEY, algorithm))
