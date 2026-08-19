import google.generativeai as genai
import os
from dotenv import load_dotenv

load_dotenv()
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "")
genai.configure(api_key=GEMINI_API_KEY)

model = genai.GenerativeModel("gemini-3.5-flash")
prompt = "Hello"
try:
    response = model.generate_content(prompt, request_options={"timeout": 2.0})
    print("Success:", repr(response.text))
except Exception as e:
    print("Exception:", str(e))
