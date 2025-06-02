import React from 'react'
import BannerSlide from '../../components/BannerSlide/BannerSlide'
import ComingSoon from '../../components/ComingSoonMovie/ComingSoon'
import PlayingMovie from '../../components/PlayingMovie/PlayingMovie'

function HomePage() {
  return (
    <>
      <BannerSlide></BannerSlide>
      <ComingSoon />
      <PlayingMovie />
    </>
  )
}

export default HomePage