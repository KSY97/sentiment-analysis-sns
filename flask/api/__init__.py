from flask import Flask
from api.view import sentiment_analysis_view, auth_view

app = Flask(__name__)
app.config['WHITE_LIST'] = ["127.0.0.1"]
app.config['JWT_SECRET_KEY'] = 'KSY_sentiment_analysis_app'
app.config['ALGORITHM'] = "HS256"
app.register_blueprint(sentiment_analysis_view.api)
app.register_blueprint(auth_view.auth)