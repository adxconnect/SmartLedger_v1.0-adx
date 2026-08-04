import os
import google.generativeai as genai
import sys

API_KEY = os.environ.get("GEMINI_API_KEY")
if not API_KEY:
    print("No API Key")
    sys.exit(0)

genai.configure(api_key=API_KEY)
model = genai.GenerativeModel("gemini-1.5-flash")

try:
    response = model.generate_content("What is the current Nifty 50 score?", tools="google_search_retrieval")
    print(response.text)
except Exception as e:
    print(f"Error with string tool: {e}")

try:
    response = model.generate_content("What is the current Nifty 50 score?", tools=[{"google_search": {}}])
    print(response.text)
except Exception as e:
    print(f"Error with dict tool: {e}")

