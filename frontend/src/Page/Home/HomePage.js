import React, { useContext } from 'react'
import BannerSlide from '../../components/BannerSlide/BannerSlide'
import ComingSoon from '../../components/ComingSoonMovie/ComingSoon'
import PlayingMovie from '../../components/PlayingMovie/PlayingMovie'
import { AuthContext } from '../../context/AuthContext'
import DefaultLayout from '../../layouts/DefaultLayout/DefaultLayout'
import MovieSchedule from '../../components/MovieSchedule/MovieSchedule'

function HomePage() {
  const { logout, token, user } = useContext(AuthContext);

  return (
    <DefaultLayout>
      <BannerSlide></BannerSlide>
      <ComingSoon />
      <PlayingMovie />
      <MovieSchedule />
    </DefaultLayout>

  )
}

export default HomePage