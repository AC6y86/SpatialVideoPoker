Sound Resources for Video Poker Game
====================================

This directory should contain the following sound files:

1. card_flip.mp3 - Sound effect for when a card is held/released
2. card_deal.mp3 - Sound effect for dealing cards
3. button_click.mp3 - Generic button click sound
4. win_small.mp3 - Sound for small wins (Jacks or Better through Three of a Kind)
5. win_big.mp3 - Sound for big wins (Full House and higher)
6. coin_insert.mp3 - Sound when placing a bet
7. cash_out.mp3 - Sound for cashing out (if implemented)
8. background_music.mp3 - Looping background casino music (optional)

File Format Requirements:
- MP3 or OGG format recommended
- Keep file sizes small (< 500KB for sound effects, < 2MB for music)
- Sample rate: 44.1kHz or 48kHz
- Bit rate: 128-192 kbps for music, 64-128 kbps for effects

Note: The SoundManager is configured but sounds won't play until these files are added.
To enable sounds, uncomment the relevant lines in SoundManager.kt after adding the files.