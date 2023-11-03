from flask import Flask
from celery import Celery, Task

def create_app() -> Flask:
    app = Flask(__name__)
    app.config.from_mapping(
        CELERY=dict(
            broker_url="redis://localhost:6379/0",
            result_backend="redis://localhost:6379/0",
            task_ignore_result=True,
        ),
    )
    app.config.from_prefixed_env()

    # Auth
    app.config['WHITE_LIST'] = ["127.0.0.1"]
    app.config['JWT_SECRET_KEY'] = "KSY_sentiment_analysis_app"
    app.config['ALGORITHM'] = "HS256"

    # Api
    # 불용어
    app.config['STOPWORDS'] = ['의','가','이','은','들','는','좀','잘','걍','과','도','를','으로','자','에','와','한','하다']
    # 최대 문장 길이 (학습 데이터 기준)
    app.config['MAX_SENTENCE_LEN'] = 30

    celery_init_app(app)

    from .view import auth_view, sentiment_analysis_view

    app.register_blueprint(auth_view.auth)
    app.register_blueprint(sentiment_analysis_view.api)
    return app


def celery_init_app(app: Flask) -> Celery:
    class FlaskTask(Task):
        def __call__(self, *args: object, **kwargs: object) -> object:
            with app.app_context():
                return self.run(*args, **kwargs)

    celery_app = Celery(app.name, task_cls=FlaskTask)
    celery_app.config_from_object(app.config["CELERY"])
    celery_app.set_default()
    app.extensions["celery"] = celery_app
    return celery_app
