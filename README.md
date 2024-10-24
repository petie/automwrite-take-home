# Automwrite TakeHome

Please find a copy of the assessment in this folder, and the text with different tones in the folder titled "different tones" in this repo.

## Overview
REST API that transfers writing styles between documents.

## Requirements
- Java 17
- Apache POI
- Claude API key
- Spring Boot

## Input Files
- Document A: Casual tone
- Document B: Formal tone
- Document C: Grandiloquent tone

## Task
1. Extract writing style from one document (e.g., Document A)
2. Upload new document with different style (e.g., Document B)
3. Process and rewrite Document B's content in Document A's style
4. Save the output

## API Endpoint
```
POST /api/transfer-style
Body: multipart/form-data
- sourceStyle (document with desired style)
- targetContent (document to rewrite)
```

## Examples
- Input 1: Doc A (Casual) + Doc B (Formal) → Output: Doc B in casual tone
- Input 2: Doc B (Formal) + Doc C (Grandiloquent) → Output: Doc C in formal tone

## Contact
If you have questions please email: Liam.Read@automwrite.co.uk or Logan.Gibson@automwrite.co.uk
