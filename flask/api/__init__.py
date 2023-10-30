from flask import Flask
from api.view import sentiment_analysis_view, auth_view

app = Flask(__name__)

# Auth
app.config['WHITE_LIST'] = ["127.0.0.1"]
app.config['JWT_SECRET_KEY'] = "KSY_sentiment_analysis_app"
app.config['ALGORITHM'] = "HS256"

# Api
# 불용어
app.config['STOPWORDS'] = ['의','가','이','은','들','는','좀','잘','걍','과','도','를','으로','자','에','와','한','하다']
# 최대 문장 길이 (학습 데이터 기준)
app.config['MAX_SENTENCE_LEN'] = 30

app.register_blueprint(sentiment_analysis_view.api)
app.register_blueprint(auth_view.auth)