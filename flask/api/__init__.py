from flask import Flask
from . import api

app = Flask(__name__)

app.register_blueprint(api.api);