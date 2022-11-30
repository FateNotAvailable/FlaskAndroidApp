from flask import Flask

app = Flask(__name__)

@app.route("/")
def index():
    return "<h1>Hello World!</h1><style>h1 {text-align: center;margin-top:25%;}</style>"

def run():
    app.run(host="127.0.0.1", port=2022, debug=False) # NOTE: Debug doesnt work on chaquopy