import asyncio
import google.generativeai as genai
import os
from dotenv import load_dotenv

load_dotenv()
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "")
genai.configure(api_key=GEMINI_API_KEY)

async def test():
    model = genai.GenerativeModel("gemini-3.5-flash")
    prompt = "You are a financial advisor. Answer: What is the difference between Old and New Tax Regime?"
    response = await model.generate_content_async(prompt)
    print("Async Text:", repr(response.text))
    
    sync_response = model.generate_content(prompt)
    print("Sync Text:", repr(sync_response.text))

asyncio.run(test())
