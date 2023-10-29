
import json
from konlpy.tag import Okt
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import load_model

# tensorflow, konlpy 설치

# 정수 인코딩한 데이터 이름
DATA_CONFIGS = 'data_configs.json'
# 불용어
stopwords = ['의','가','이','은','들','는','좀','잘','걍','과','도','를','으로','자','에','와','한','하다']
# 최대 문장 길이 (학습 데이터 기준)
max_len = 30

okt = Okt()
tokenizer = Tokenizer()

loaded_model = load_model('./static/best_model.h5')
prepro_configs = json.load(open('./static/'+DATA_CONFIGS, 'r', encoding='UTF-8'))

def sentiment_predict(new_sentence):
    loaded_model = load_model('./static/best_model.h5')
    prepro_configs = json.load(open('./static/'+DATA_CONFIGS, 'r', encoding='UTF-8'))
    word_vocab = prepro_configs['vocab']
    tokenizer.fit_on_texts(word_vocab)
    new_sentence = okt.morphs(new_sentence, stem=True) # 토큰화
    new_sentence = [word for word in new_sentence if not word in stopwords] # 불용어 제거
    encoded = tokenizer.texts_to_sequences([new_sentence]) # 정수 인코딩
    pad_new = pad_sequences(encoded, maxlen = max_len) # 패딩
    score = float(loaded_model.predict(pad_new)) # 예측
    if(score > 0.5):
        data = {'predict' : 'positive', 'score' : '{:.2f}'.format(score * 100)}
        return data
    else:
        data = {'predict' : 'negative', 'score' : '{:.2f}'.format((1-score) * 100)}
        return data
