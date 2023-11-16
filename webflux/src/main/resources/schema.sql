CREATE TABLE IF NOT EXISTS member
(
    member_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    positive_rate FLOAT,
    registered_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS post
(
    post_id SERIAL PRIMARY KEY,
    member_id BIGINT UNSIGNED NOT NULL,
    contents VARCHAR(255) NOT NULL,
    predict_result VARCHAR(50),
    predict_percent FLOAT,
    wrote_at DATETIME NOT NULL,
    edited_at DATETIME,
    FOREIGN KEY (member_id)
    REFERENCES member(member_id) ON UPDATE CASCADE ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS comment
(
    comment_id SERIAL PRIMARY KEY,
    member_id BIGINT UNSIGNED NOT NULL,
    post_id BIGINT UNSIGNED NOT NULL,
    contents VARCHAR(255),
    predict_result VARCHAR(50),
    predict_percent FLOAT,
    wrote_at DATETIME,
    edited_at DATETIME,
    FOREIGN KEY (member_id)
    REFERENCES member(member_id) ON UPDATE CASCADE ON DELETE NO ACTION,
    FOREIGN KEY (post_id)
    REFERENCES post(post_id) ON UPDATE CASCADE ON DELETE NO ACTION
);