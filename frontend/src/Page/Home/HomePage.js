import React, { useContext } from 'react'
import BannerSlide from '../../components/BannerSlide/BannerSlide'
import ComingSoon from '../../components/ComingSoonMovie/ComingSoon'
import PlayingMovie from '../../components/PlayingMovie/PlayingMovie'
import { AuthContext } from '../../context/AuthContext'

function HomePage() {
  const { logout, token, user } = useContext(AuthContext);

  return (
    <>
      <BannerSlide></BannerSlide>
      <ComingSoon />
      <PlayingMovie />
    </>
  )
}

export default HomePage