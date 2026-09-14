import os
import time
import json
import base64
import wave
import mimetypes

from dotenv import load_dotenv
from google import genai
from google.genai import types


# =========================================================
# ENVIRONMENT
# =========================================================

load_dotenv()

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

if not GEMINI_API_KEY:
    raise RuntimeError(
        "GEMINI_API_KEY not found in environment variables."
    )


# =========================================================
# GEMINI CLIENT
# =========================================================

client = genai.Client(
    api_key=GEMINI_API_KEY
)


# =========================================================
# MODEL CONFIGURATION
# =========================================================

TRANSCRIPTION_MODEL = "gemini-3.5-transcribe"

ASSISTANT_MODEL = os.getenv(
    "GEMINI_ASSISTANT_MODEL",
    "gemini-3.5-flash"
)

CATALOG_MODEL = os.getenv(
    "GEMINI_CATALOG_MODEL",
    "gemini-3.5-flash"
)

TTS_MODEL = os.getenv(
    "GEMINI_TTS_MODEL",
    "gemini-3.1-flash-tts-preview"
)


# =========================================================
# FILE / AUDIO HELPERS
# =========================================================

def get_audio_mime_type(audio_path: str) -> str:
    """
    Detect the audio MIME type.

    Some .mpeg files can incorrectly be detected as
    video/mpeg, so force MPEG audio to audio/mpeg.
    """

    extension = os.path.splitext(
        audio_path
    )[1].lower()

    mime_map = {

        ".mp3": "audio/mpeg",

        ".mpeg": "audio/mpeg",

        ".mpga": "audio/mpeg",

        ".wav": "audio/wav",

        ".m4a": "audio/m4a",

        ".aac": "audio/aac",

        ".ogg": "audio/ogg",

        ".flac": "audio/flac",

        ".opus": "audio/opus",

        ".webm": "audio/webm",

    }

    if extension in mime_map:
        return mime_map[extension]

    guessed_type, _ = mimetypes.guess_type(
        audio_path
    )

    if guessed_type and guessed_type.startswith(
        "audio/"
    ):
        return guessed_type

    return "audio/mpeg"


# =========================================================
# WAIT FOR GEMINI FILE TO BECOME ACTIVE
# =========================================================

def wait_for_file_active(
    uploaded_file,
    timeout_seconds: int = 60,
    poll_seconds: int = 2
):
    """
    Gemini Files API uploads can initially be PROCESSING.

    We must wait until the file becomes ACTIVE before
    using it for inference.
    """

    print(
        "Uploaded Gemini file:",
        uploaded_file.name
    )

    start_time = time.time()

    while True:

        state = uploaded_file.state

        state_name = getattr(
            state,
            "name",
            str(state)
        )

        print(
            "Gemini file state:",
            state_name
        )

        # ---------------------------------------------
        # READY
        # ---------------------------------------------

        if state_name == "ACTIVE":

            print(
                "Gemini audio file is ACTIVE."
            )

            return uploaded_file

        # ---------------------------------------------
        # FAILED
        # ---------------------------------------------

        if state_name == "FAILED":

            error_message = getattr(
                uploaded_file,
                "error",
                None
            )

            raise RuntimeError(
                "Gemini audio file processing failed: "
                f"{error_message}"
            )

        # ---------------------------------------------
        # TIMEOUT
        # ---------------------------------------------

        if (
            time.time() - start_time
            > timeout_seconds
        ):

            raise TimeoutError(
                "Timed out waiting for Gemini "
                "audio file to become ACTIVE."
            )

        # ---------------------------------------------
        # WAIT
        # ---------------------------------------------

        time.sleep(
            poll_seconds
        )

        uploaded_file = client.files.get(
            name=uploaded_file.name
        )


# =========================================================
# UPLOAD AUDIO
# =========================================================

def upload_audio_file(
    audio_path: str
):
    """
    Upload audio to Gemini Files API and wait
    until the file is ACTIVE.
    """

    if not os.path.exists(audio_path):

        raise FileNotFoundError(
            f"Audio file not found: {audio_path}"
        )

    mime_type = get_audio_mime_type(
        audio_path
    )

    print(
        "Audio MIME type:",
        mime_type
    )

    print(
        "Uploading audio to Gemini..."
    )

    uploaded_file = client.files.upload(

        file=audio_path,

        config=types.UploadFileConfig(
            mime_type=mime_type
        )

    )

    print(
        "Upload successful."
    )

    uploaded_file = wait_for_file_active(
        uploaded_file
    )

    return uploaded_file, mime_type


# =========================================================
# LANGUAGE DETECTION
# =========================================================

def detect_language(
    text: str
):
    """
    Lightweight script-based language detection.

    Gemini handles the actual speech recognition.
    This helper identifies common Indian languages
    from the resulting text for the prototype.
    """

    if not text:
        return (
            "Unknown",
            "unknown"
        )

    # ---------------------------------------------
    # TELUGU
    # ---------------------------------------------

    if any(
        "\u0C00"
        <= char
        <= "\u0C7F"
        for char in text
    ):

        return (
            "Telugu",
            "te-IN"
        )

    # ---------------------------------------------
    # HINDI / DEVANAGARI
    # ---------------------------------------------

    if any(
        "\u0900"
        <= char
        <= "\u097F"
        for char in text
    ):

        return (
            "Hindi",
            "hi-IN"
        )

    # ---------------------------------------------
    # KANNADA
    # ---------------------------------------------

    if any(
        "\u0C80"
        <= char
        <= "\u0CFF"
        for char in text
    ):

        return (
            "Kannada",
            "kn-IN"
        )

    # ---------------------------------------------
    # TAMIL
    # ---------------------------------------------

    if any(
        "\u0B80"
        <= char
        <= "\u0BFF"
        for char in text
    ):

        return (
            "Tamil",
            "ta-IN"
        )

    # ---------------------------------------------
    # MALAYALAM
    # ---------------------------------------------

    if any(
        "\u0D00"
        <= char
        <= "\u0D7F"
        for char in text
    ):

        return (
            "Malayalam",
            "ml-IN"
        )

    # ---------------------------------------------
    # ENGLISH / LATIN
    # ---------------------------------------------

    if any(
        ("a" <= char.lower() <= "z")
        for char in text
    ):

        return (
            "English",
            "en-IN"
        )

    return (
        "Unknown",
        "unknown"
    )


# =========================================================
# VOICE TRANSCRIPTION
# =========================================================

def transcribe_voice(
    audio_path: str
):
    """
    Convert artisan speech into text using
    Gemini 3.5 Transcribe.

    Automatic language identification is enabled.
    """

    print(
        "\n================================"
    )

    print(
        "       GEMINI TRANSCRIPTION"
    )

    print(
        "================================"
    )

    uploaded_file = None

    try:

        uploaded_file, mime_type = upload_audio_file(
            audio_path
        )

        print(
            "Starting Gemini transcription..."
        )

        interaction = client.interactions.create(

            model=TRANSCRIPTION_MODEL,

            input=[

                {
                    "type": "audio",

                    "uri": uploaded_file.uri,

                    "mime_type": mime_type
                }

            ],

            generation_config={

                "transcription_config": {

                    # Empty list = automatic
                    # language detection

                    "language_codes": [],

                    "mode": "smart"

                }

            }

        )

        transcription = (
            interaction.output_text
            or ""
        ).strip()

        if not transcription:

            raise RuntimeError(
                "Gemini returned an empty transcription."
            )

        language, language_code = detect_language(
            transcription
        )

        print(
            "Transcription:",
            transcription
        )

        print(
            "Detected language:",
            language
        )

        print(
            "Language code:",
            language_code
        )

        return {

            "transcription":
                transcription,

            "language":
                language,

            "language_code":
                language_code

        }

    finally:

        # -----------------------------------------
        # DELETE TEMPORARY GEMINI FILE
        # -----------------------------------------

        if uploaded_file:

            try:

                client.files.delete(
                    name=uploaded_file.name
                )

                print(
                    "Gemini temporary file deleted."
                )

            except Exception as cleanup_error:

                print(
                    "Gemini file cleanup warning:",
                    cleanup_error
                )


# =========================================================
# AI VOICE ASSISTANT RESPONSE
# =========================================================

def generate_voice_response(
    transcription: str,
    language: str
):
    """
    Generate an AI response to the artisan's
    spoken question/request.
    """

    if not transcription:

        return (
            "Sorry, I could not understand the audio."
        )

    language_instruction = {

        "Telugu":
            "Respond naturally in Telugu.",

        "Hindi":
            "Respond naturally in Hindi.",

        "Tamil":
            "Respond naturally in Tamil.",

        "Kannada":
            "Respond naturally in Kannada.",

        "Malayalam":
            "Respond naturally in Malayalam.",

        "English":
            "Respond naturally in English."

    }.get(
        language,
        "Respond naturally in the same language as the user."
    )

    prompt = f"""
You are an AI assistant for marginalized artisans
who sell handmade products.

The artisan said:

"{transcription}"

Detected language:
{language}

Instructions:

1. {language_instruction}
2. Keep the response short and useful.
3. Use simple language.
4. Help the artisan with products, cataloging,
   pricing, markets, customers or selling.
5. Do not invent specific government schemes,
   prices or market orders.
6. If the request is unclear, ask a short
   clarification question.
"""

    print(
        "Generating AI voice assistant response..."
    )

    response = client.models.generate_content(

        model=ASSISTANT_MODEL,

        contents=prompt

    )

    result = (
        response.text
        or ""
    ).strip()

    if not result:

        raise RuntimeError(
            "AI assistant returned an empty response."
        )

    print(
        "AI response:",
        result
    )

    return result


# =========================================================
# TEXT TO SPEECH
# =========================================================

def save_wave_file(
    filename: str,
    pcm_data: bytes,
    channels: int = 1,
    rate: int = 24000,
    sample_width: int = 2
):
    """
    Save Gemini PCM audio as WAV.
    """

    with wave.open(
        filename,
        "wb"
    ) as wave_file:

        wave_file.setnchannels(
            channels
        )

        wave_file.setsampwidth(
            sample_width
        )

        wave_file.setframerate(
            rate
        )

        wave_file.writeframes(
            pcm_data
        )


def generate_speech(
    text: str,
    output_path: str = "/tmp/voice_assistant_response.wav"
):
    """
    Convert AI response text into speech.

    Gemini TTS returns PCM audio which is stored
    as a WAV file.
    """

    print(
        "Generating voice response..."
    )

    tts_prompt = f"""
Speak this response naturally and clearly:

{text}
"""

    interaction = client.interactions.create(

        model=TTS_MODEL,

        input=tts_prompt,

        response_format={
            "type": "audio"
        },

        generation_config={

            "speech_config": [

                {
                    "voice": "Kore"
                }

            ]

        }

    )

    audio_data = (
        interaction.output_audio.data
    )

    # Gemini may return base64 encoded audio
    # through the Interactions API.

    if isinstance(
        audio_data,
        str
    ):

        audio_data = base64.b64decode(
            audio_data
        )

    save_wave_file(
        output_path,
        audio_data
    )

    print(
        "Voice response saved:",
        output_path
    )

    return output_path


# =========================================================
# COMPLETE VOICE ASSISTANT
# =========================================================

def voice_assistant(
    audio_path: str
):
    """
    Complete voice assistant pipeline:

    Audio
       ↓
    Speech-to-text
       ↓
    Language detection
       ↓
    AI response
       ↓
    Text-to-speech
       ↓
    WAV audio
    """

    print(
        "\n========================================"
    )

    print(
        "       AI VOICE ASSISTANT"
    )

    print(
        "========================================"
    )

    # -----------------------------------------
    # STEP 1: TRANSCRIPTION
    # -----------------------------------------

    transcription_result = transcribe_voice(
        audio_path
    )

    transcription = (
        transcription_result[
            "transcription"
        ]
    )

    language = (
        transcription_result[
            "language"
        ]
    )

    language_code = (
        transcription_result[
            "language_code"
        ]
    )

    # -----------------------------------------
    # STEP 2: AI RESPONSE
    # -----------------------------------------

    response = generate_voice_response(

        transcription,

        language

    )

    # -----------------------------------------
    # STEP 3: TEXT TO SPEECH
    # -----------------------------------------

    audio_output = generate_speech(
        response
    )

    print(
        "Voice assistant completed."
    )

    return {

        "language":
            language,

        "language_code":
            language_code,

        "transcription":
            transcription,

        "response":
            response,

        "audio_path":
            audio_output

    }


# =========================================================
# VOICE CATALOGING
# =========================================================

def voice_catalog(
    transcription: str,
    language: str = "Unknown"
):
    """
    Convert an artisan's spoken product information
    into a structured product catalog.

    Example:

    "This is a traditional bamboo basket..."

    becomes structured catalog information.
    """

    if not transcription:

        raise ValueError(
            "Transcription is empty."
        )

    print(
        "\n========================================"
    )

    print(
        "       AI VOICE CATALOGING"
    )

    print(
        "========================================"
    )

    prompt = f"""
You are an AI cataloging assistant for
marginalized Indian artisans.

Convert the artisan's spoken description
into a structured product catalog.

Artisan language:
{language}

Artisan speech:
{transcription}

Extract only information that is supported
by the artisan's speech.

Return:

- product_name
- category
- product_type
- material
- color
- style
- features
- keywords
- description
- artisan_notes

If information is missing, use an empty
string or empty list.

Do not invent information.
"""

    schema = {

        "type": "OBJECT",

        "properties": {

            "product_name": {
                "type": "STRING"
            },

            "category": {
                "type": "STRING"
            },

            "product_type": {
                "type": "STRING"
            },

            "material": {

                "type": "ARRAY",

                "items": {
                    "type": "STRING"
                }

            },

            "color": {

                "type": "ARRAY",

                "items": {
                    "type": "STRING"
                }

            },

            "style": {
                "type": "STRING"
            },

            "features": {

                "type": "ARRAY",

                "items": {
                    "type": "STRING"
                }

            },

            "keywords": {

                "type": "ARRAY",

                "items": {
                    "type": "STRING"
                }

            },

            "description": {
                "type": "STRING"
            },

            "artisan_notes": {
                "type": "STRING"
            }

        },

        "required": [

            "product_name",

            "category",

            "product_type",

            "material",

            "color",

            "style",

            "features",

            "keywords",

            "description",

            "artisan_notes"

        ]

    }

    response = client.models.generate_content(

        model=CATALOG_MODEL,

        contents=prompt,

        config={

            "response_mime_type":
                "application/json",

            "response_json_schema":
                schema

        }

    )

    result_text = (
        response.text
        or ""
    ).strip()

    if not result_text:

        raise RuntimeError(
            "Gemini returned an empty catalog."
        )

    try:

        catalog = json.loads(
            result_text
        )

    except json.JSONDecodeError as error:

        raise RuntimeError(
            "Gemini returned invalid catalog JSON: "
            f"{result_text}"
        ) from error

    print(
        "Catalog generated successfully."
    )

    print(
        json.dumps(
            catalog,
            indent=2,
            ensure_ascii=False
        )
    )

    return catalog


# =========================================================
# SIMPLE TEST
# =========================================================

if __name__ == "__main__":

    print(
        "voice.py loaded successfully."
    )

    print(
        "Transcription model:",
        TRANSCRIPTION_MODEL
    )

    print(
        "Assistant model:",
        ASSISTANT_MODEL
    )

    print(
        "Catalog model:",
        CATALOG_MODEL
    )

    print(
        "TTS model:",
        TTS_MODEL
    )